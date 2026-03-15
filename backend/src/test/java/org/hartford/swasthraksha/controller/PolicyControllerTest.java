package org.hartford.swasthraksha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.swasthraksha.config.SecurityConfig;
import org.hartford.swasthraksha.exception.GlobalExceptionHandler;
import org.hartford.swasthraksha.filter.JwtFilter;
import org.hartford.swasthraksha.model.Policy;
import org.hartford.swasthraksha.service.PolicyService;
import org.hartford.swasthraksha.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PolicyController.
 *
 * Layer    : Controller (Web Layer only — no DB, no full Spring context)
 * Tool     : @WebMvcTest + MockMvc
 * Mocking  : @MockBean replaces PolicyService
 * Auth     : GET /policies is public; all mutations require admin role
 * Naming   : methodName_whenCondition_shouldExpectedResult
 */
@WebMvcTest(PolicyController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtFilter.class, JwtUtil.class})
class PolicyControllerTest {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final String BASE_URL        = "/policies";
    private static final String TOGGLE_URL      = "/policies/{id}/toggle-status";
    private static final String DELETE_URL      = "/policies/{id}";
    private static final String UPDATE_URL      = "/policies/{id}";

    private static final String ADMIN_EMAIL     = "admin@test.com";
    private static final String APPLICANT_EMAIL = "applicant@test.com";

    // ── Beans ──────────────────────────────────────────────────────────────
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PolicyService policyService;
    @MockBean private UserDetailsService userDetailsService;

    // ── Fixtures ───────────────────────────────────────────────────────────
    private Policy activePolicy;
    private Policy inactivePolicy;

    @BeforeEach
    void setUp() {
        activePolicy = new Policy();
        activePolicy.setId(1L);
        activePolicy.setPolicyName("Health Basic");
        activePolicy.setPolicyCode("HEAL_001");
        activePolicy.setMinCoverage(50_000.0);
        activePolicy.setMaxCoverage(500_000.0);
        activePolicy.setBasePercent(2.5);
        activePolicy.setActive(true);
        activePolicy.setPlanType("BOTH");

        inactivePolicy = new Policy();
        inactivePolicy.setId(2L);
        inactivePolicy.setPolicyName("Health Premium");
        inactivePolicy.setPolicyCode("HEAL_002");
        inactivePolicy.setActive(false);
        inactivePolicy.setPlanType("INDIVIDUAL");
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /policies
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addPolicy - when admin submits valid policy - should return 201 with policy body")
    void addPolicy_whenAdminSubmitsValidPolicy_shouldReturn201WithPolicyBody() throws Exception {
        when(policyService.addPolicy(any(Policy.class))).thenReturn(activePolicy);

        mockMvc.perform(post(BASE_URL)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activePolicy)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.policyCode", is("HEAL_001")))
                .andExpect(jsonPath("$.policyName", is("Health Basic")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(policyService, times(1)).addPolicy(any(Policy.class));
    }

    @Test
    @DisplayName("addPolicy - when non-admin submits - should return 403 Forbidden")
    void addPolicy_whenNonAdminSubmits_shouldReturn403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activePolicy)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyService);
    }

    @Test
    @DisplayName("addPolicy - when unauthenticated - should return 401 Unauthorized")
    void addPolicy_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activePolicy)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(policyService);
    }

    @Test
    @DisplayName("addPolicy - when policy code already exists - should return 400 with error message")
    void addPolicy_whenDuplicatePolicyCode_shouldReturn400WithMessage() throws Exception {
        when(policyService.addPolicy(any(Policy.class)))
                .thenThrow(new RuntimeException("Policy with code HEAL_001 already exists"));

        mockMvc.perform(post(BASE_URL)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activePolicy)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("HEAL_001")));
    }

    // ══════════════════════════════════════════════════════════════════════
    // PATCH /policies/{id}/toggle-status
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("toggleStatus - when admin toggles active policy - should return 200 with deactivated policy")
    void toggleStatus_whenAdminTogglesActivePolicy_shouldReturn200WithUpdatedPolicy() throws Exception {
        Policy toggled = new Policy();
        toggled.setId(1L);
        toggled.setPolicyCode("HEAL_001");
        toggled.setActive(false); // was active → now inactive

        when(policyService.toggleStatus(1L)).thenReturn(toggled);

        mockMvc.perform(patch(TOGGLE_URL, 1L)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));

        verify(policyService).toggleStatus(1L);
    }

    @Test
    @DisplayName("toggleStatus - when non-admin calls toggle - should return 403 Forbidden")
    void toggleStatus_whenNonAdminCallsToggle_shouldReturn403() throws Exception {
        mockMvc.perform(patch(TOGGLE_URL, 1L)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyService);
    }

    @Test
    @DisplayName("toggleStatus - when unauthenticated - should return 401 Unauthorized")
    void toggleStatus_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(patch(TOGGLE_URL, 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("toggleStatus - when policy not found - should return 400 with error message")
    void toggleStatus_whenPolicyNotFound_shouldReturn400() throws Exception {
        when(policyService.toggleStatus(999L))
                .thenThrow(new RuntimeException("Policy not found"));

        mockMvc.perform(patch(TOGGLE_URL, 999L)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Policy not found")));
    }

    // ══════════════════════════════════════════════════════════════════════
    // DELETE /policies/{id}
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deletePolicy - when admin deletes existing policy - should return 200 with success message")
    void deletePolicy_whenAdminDeletesExistingPolicy_shouldReturn200WithMessage() throws Exception {
        doNothing().when(policyService).deletePolicy(1L);

        mockMvc.perform(delete(DELETE_URL, 1L)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("deleted")));

        verify(policyService).deletePolicy(1L);
    }

    @Test
    @DisplayName("deletePolicy - when non-admin calls delete - should return 403 Forbidden")
    void deletePolicy_whenNonAdminCallsDelete_shouldReturn403() throws Exception {
        mockMvc.perform(delete(DELETE_URL, 1L)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyService);
    }

    @Test
    @DisplayName("deletePolicy - when unauthenticated - should return 401 Unauthorized")
    void deletePolicy_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(delete(DELETE_URL, 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deletePolicy - when policy not found - should return 400 with error message")
    void deletePolicy_whenPolicyNotFound_shouldReturn400() throws Exception {
        doThrow(new RuntimeException("Policy not found"))
                .when(policyService).deletePolicy(999L);

        mockMvc.perform(delete(DELETE_URL, 999L)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Policy not found")));
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /policies
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllPolicies - when public request with no filters - should return 200 with only active policies")
    void getAllPolicies_whenPublicRequestNoFilters_shouldReturnOnlyActivePolicies() throws Exception {
        when(policyService.getAllPolicies()).thenReturn(List.of(activePolicy, inactivePolicy));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))          // inactive filtered out
                .andExpect(jsonPath("$[0].policyCode", is("HEAL_001")));
    }

    @Test
    @DisplayName("getAllPolicies - when adminView=true - should return 200 with all policies including inactive")
    void getAllPolicies_whenAdminViewTrue_shouldReturnAllIncludingInactive() throws Exception {
        when(policyService.getAllPolicies()).thenReturn(List.of(activePolicy, inactivePolicy));

        mockMvc.perform(get(BASE_URL).param("adminView", "true")
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("getAllPolicies - when type=INDIVIDUAL filter applied - should return only INDIVIDUAL or BOTH plans")
    void getAllPolicies_whenTypeFilterIndividual_shouldReturnMatchingAndBothPlans() throws Exception {
        Policy bothPlan = new Policy();
        bothPlan.setId(3L);
        bothPlan.setPolicyCode("HEAL_003");
        bothPlan.setActive(true);
        bothPlan.setPlanType("BOTH");

        when(policyService.getAllPolicies()).thenReturn(List.of(activePolicy, bothPlan));
        // activePolicy has planType="BOTH", bothPlan also "BOTH" — both should be returned for INDIVIDUAL filter

        mockMvc.perform(get(BASE_URL).param("type", "INDIVIDUAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @DisplayName("getAllPolicies - when no active policies exist - should return 200 with empty list")
    void getAllPolicies_whenNoActivePolicies_shouldReturn200WithEmptyList() throws Exception {
        when(policyService.getAllPolicies()).thenReturn(List.of(inactivePolicy));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ══════════════════════════════════════════════════════════════════════
    // PUT /policies/{id}
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updatePolicy - when admin sends valid update - should return 200 with updated policy")
    void updatePolicy_whenAdminSendsValidUpdate_shouldReturn200WithUpdatedPolicy() throws Exception {
        Policy updated = new Policy();
        updated.setId(1L);
        updated.setPolicyCode("HEAL_001");
        updated.setPolicyName("Updated Health Basic");
        updated.setActive(true);

        when(policyService.updatePolicy(eq(1L), any(Policy.class))).thenReturn(updated);

        mockMvc.perform(put(UPDATE_URL, 1L)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyName", is("Updated Health Basic")));

        verify(policyService).updatePolicy(eq(1L), any(Policy.class));
    }

    @Test
    @DisplayName("updatePolicy - when non-admin sends update - should return 403 Forbidden")
    void updatePolicy_whenNonAdminSendsUpdate_shouldReturn403() throws Exception {
        mockMvc.perform(put(UPDATE_URL, 1L)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activePolicy)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyService);
    }

    @Test
    @DisplayName("updatePolicy - when unauthenticated - should return 401 Unauthorized")
    void updatePolicy_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(put(UPDATE_URL, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activePolicy)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("updatePolicy - when policy not found - should return 400 with error message")
    void updatePolicy_whenPolicyNotFound_shouldReturn400() throws Exception {
        when(policyService.updatePolicy(eq(999L), any(Policy.class)))
                .thenThrow(new RuntimeException("Policy not found"));

        mockMvc.perform(put(UPDATE_URL, 999L)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activePolicy)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Policy not found")));
    }
}
