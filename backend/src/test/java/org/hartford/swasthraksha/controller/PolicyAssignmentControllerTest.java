package org.hartford.swasthraksha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.swasthraksha.config.SecurityConfig;
import org.hartford.swasthraksha.exception.GlobalExceptionHandler;
import org.hartford.swasthraksha.filter.JwtFilter;
import org.hartford.swasthraksha.model.PolicyAssignment;
import org.hartford.swasthraksha.model.PolicyStatus;
import org.hartford.swasthraksha.model.Users;
import org.hartford.swasthraksha.service.PolicyAssignmentService;
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

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PolicyAssignmentController.
 *
 * Layer    : Controller (Web Layer only — no DB, no full Spring context)
 * Tool     : @WebMvcTest + MockMvc
 * Mocking  : @MockBean replaces PolicyAssignmentService
 * Auth     : APPLICANT for user endpoints, admin for /all
 * Naming   : methodName_whenCondition_shouldExpectedResult
 */
@WebMvcTest(PolicyAssignmentController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtFilter.class, JwtUtil.class})
class PolicyAssignmentControllerTest {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final String MY_POLICIES_URL    = "/policyassignments/my";
    private static final String PAY_URL            = "/policyassignments/{id}/pay";
    private static final String CREATE_URL         = "/policyassignments";
    private static final String ALL_URL            = "/policyassignments/all";

    private static final String APPLICANT_EMAIL    = "applicant@test.com";
    private static final String ADMIN_EMAIL        = "admin@test.com";
    private static final String UNDERWRITER_EMAIL  = "underwriter@test.com";

    // ── Beans ──────────────────────────────────────────────────────────────
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PolicyAssignmentService policyAssignmentService;
    @MockBean private UserDetailsService userDetailsService;

    // ── Fixtures ───────────────────────────────────────────────────────────
    private PolicyAssignment activeAssignment;
    private PolicyAssignment pendingAssignment;

    @BeforeEach
    void setUp() {
        Users user = new Users();
        user.setId(1L);
        user.setEmail(APPLICANT_EMAIL);

        activeAssignment = new PolicyAssignment();
        activeAssignment.setId(1L);
        activeAssignment.setPolicyNumber("POL-ABCD1234");
        activeAssignment.setStatus(PolicyStatus.ACTIVE);
        activeAssignment.setCoverageAmount(200_000.0);
        activeAssignment.setPremiumAmount(5_000.0);
        activeAssignment.setPremiumPaid(5_000.0);
        activeAssignment.setStartDate(LocalDate.now());
        activeAssignment.setEndDate(LocalDate.now().plusYears(3));
        activeAssignment.setUser(user);

        pendingAssignment = new PolicyAssignment();
        pendingAssignment.setId(2L);
        pendingAssignment.setPolicyNumber("POL-EFGH5678");
        pendingAssignment.setStatus(PolicyStatus.PENDING_PAYMENT);
        pendingAssignment.setCoverageAmount(150_000.0);
        pendingAssignment.setPremiumAmount(4_000.0);
        pendingAssignment.setUser(user);
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /policyassignments/my
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getMyPolicies - when applicant requests own policies - should return 200 with list")
    void getMyPolicies_whenApplicantRequests_shouldReturn200WithList() throws Exception {
        when(policyAssignmentService.getUserPolicies(APPLICANT_EMAIL))
                .thenReturn(List.of(activeAssignment));

        mockMvc.perform(get(MY_POLICIES_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].policyNumber", is("POL-ABCD1234")))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));

        verify(policyAssignmentService).getUserPolicies(APPLICANT_EMAIL);
    }

    @Test
    @DisplayName("getMyPolicies - when applicant has no policies - should return 200 with empty list")
    void getMyPolicies_whenApplicantHasNoPolicies_shouldReturn200WithEmptyList() throws Exception {
        when(policyAssignmentService.getUserPolicies(APPLICANT_EMAIL)).thenReturn(List.of());

        mockMvc.perform(get(MY_POLICIES_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("getMyPolicies - when non-applicant (underwriter) requests - should return 403 Forbidden")
    void getMyPolicies_whenUnderwriterRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(MY_POLICIES_URL)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyAssignmentService);
    }

    @Test
    @DisplayName("getMyPolicies - when unauthenticated - should return 401 Unauthorized")
    void getMyPolicies_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(MY_POLICIES_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(policyAssignmentService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // PATCH /policyassignments/{id}/pay
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("payPolicy - when applicant pays pending policy - should return 200 with active assignment")
    void payPolicy_whenApplicantPaysPendingPolicy_shouldReturn200WithActiveAssignment() throws Exception {
        when(policyAssignmentService.makePayment(2L, APPLICANT_EMAIL)).thenReturn(activeAssignment);

        mockMvc.perform(patch(PAY_URL, 2L)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(policyAssignmentService).makePayment(2L, APPLICANT_EMAIL);
    }

    @Test
    @DisplayName("payPolicy - when non-applicant calls pay - should return 403 Forbidden")
    void payPolicy_whenNonApplicantCalls_shouldReturn403() throws Exception {
        mockMvc.perform(patch(PAY_URL, 2L)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyAssignmentService);
    }

    @Test
    @DisplayName("payPolicy - when unauthenticated - should return 401 Unauthorized")
    void payPolicy_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(patch(PAY_URL, 2L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("payPolicy - when policy not found - should return 400 with error message")
    void payPolicy_whenPolicyNotFound_shouldReturn400() throws Exception {
        when(policyAssignmentService.makePayment(999L, APPLICANT_EMAIL))
                .thenThrow(new RuntimeException("Policy not found"));

        mockMvc.perform(patch(PAY_URL, 999L)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Policy not found")));
    }

    @Test
    @DisplayName("payPolicy - when policy already active (not PENDING_PAYMENT) - should return 400")
    void payPolicy_whenPolicyAlreadyActive_shouldReturn400() throws Exception {
        when(policyAssignmentService.makePayment(1L, APPLICANT_EMAIL))
                .thenThrow(new RuntimeException("Policy is not in PENDING_PAYMENT status"));

        mockMvc.perform(patch(PAY_URL, 1L)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("PENDING_PAYMENT")));
    }

    @Test
    @DisplayName("payPolicy - when policy belongs to another user - should return 400 Unauthorized")
    void payPolicy_whenPolicyBelongsToAnotherUser_shouldReturn400() throws Exception {
        when(policyAssignmentService.makePayment(2L, APPLICANT_EMAIL))
                .thenThrow(new RuntimeException("Unauthorized: policy does not belong to user"));

        mockMvc.perform(patch(PAY_URL, 2L)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Unauthorized")));
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /policyassignments  (First payment / create assignment)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createPolicyAssignment - when applicant submits valid application number - should return 201 with assignment")
    void createPolicyAssignment_whenValidApplicationNumber_shouldReturn201WithAssignment() throws Exception {
        when(policyAssignmentService.createPolicyAssignment("APP-2026-0001", APPLICANT_EMAIL))
                .thenReturn(activeAssignment);

        mockMvc.perform(post(CREATE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"APP-2026-0001\""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.policyNumber", is("POL-ABCD1234")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(policyAssignmentService).createPolicyAssignment("APP-2026-0001", APPLICANT_EMAIL);
    }

    @Test
    @DisplayName("createPolicyAssignment - when non-applicant submits - should return 403 Forbidden")
    void createPolicyAssignment_whenNonApplicantSubmits_shouldReturn403() throws Exception {
        mockMvc.perform(post(CREATE_URL)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"APP-2026-0001\""))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyAssignmentService);
    }

    @Test
    @DisplayName("createPolicyAssignment - when unauthenticated - should return 401 Unauthorized")
    void createPolicyAssignment_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post(CREATE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"APP-2026-0001\""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("createPolicyAssignment - when application not found - should return 400 with error message")
    void createPolicyAssignment_whenApplicationNotFound_shouldReturn400() throws Exception {
        when(policyAssignmentService.createPolicyAssignment("APP-XXXX", APPLICANT_EMAIL))
                .thenThrow(new RuntimeException("Application not found: APP-XXXX"));

        mockMvc.perform(post(CREATE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"APP-XXXX\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("APP-XXXX")));
    }

    @Test
    @DisplayName("createPolicyAssignment - when application not in CUSTOMER_ACCEPTED status - should return 400")
    void createPolicyAssignment_whenApplicationNotCustomerAccepted_shouldReturn400() throws Exception {
        when(policyAssignmentService.createPolicyAssignment("APP-2026-0001", APPLICANT_EMAIL))
                .thenThrow(new RuntimeException("Application must be in CUSTOMER_ACCEPTED status to make first payment"));

        mockMvc.perform(post(CREATE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"APP-2026-0001\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("CUSTOMER_ACCEPTED")));
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /policyassignments/all
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllPolicies - when admin requests all assignments - should return 200 with full list")
    void getAllPolicies_whenAdminRequests_shouldReturn200WithFullList() throws Exception {
        when(policyAssignmentService.getAllPolicies())
                .thenReturn(List.of(activeAssignment, pendingAssignment));

        mockMvc.perform(get(ALL_URL)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(policyAssignmentService).getAllPolicies();
    }

    @Test
    @DisplayName("getAllPolicies - when non-admin (applicant) requests all - should return 403 Forbidden")
    void getAllPolicies_whenNonAdminRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(ALL_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(policyAssignmentService);
    }

    @Test
    @DisplayName("getAllPolicies - when unauthenticated - should return 401 Unauthorized")
    void getAllPolicies_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(ALL_URL))
                .andExpect(status().isUnauthorized());
    }
}
