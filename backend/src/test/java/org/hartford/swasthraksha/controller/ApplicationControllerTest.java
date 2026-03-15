package org.hartford.swasthraksha.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.swasthraksha.config.SecurityConfig;
import org.hartford.swasthraksha.dto.ApplicationRequest;
import org.hartford.swasthraksha.dto.PolicyMemberRequest;
import org.hartford.swasthraksha.dto.UnderwriterDecisionRequest;
import org.hartford.swasthraksha.exception.GlobalExceptionHandler;
import org.hartford.swasthraksha.filter.JwtFilter;
import org.hartford.swasthraksha.model.Application;
import org.hartford.swasthraksha.model.ApplicationStatus;
import org.hartford.swasthraksha.model.PolicyMember;
import org.hartford.swasthraksha.model.Relationship;
import org.hartford.swasthraksha.service.ApplicationService;
import org.hartford.swasthraksha.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ApplicationController.
 *
 * Layer    : Controller (Web Layer only — no DB, no full Spring context)
 * Tool     : @WebMvcTest + MockMvc
 * Mocking  : @MockBean replaces ApplicationService
 * Auth     : SecurityMockMvcRequestPostProcessors.user() simulates JWT roles
 * Naming   : methodName_whenCondition_shouldExpectedResult
 */
@WebMvcTest(ApplicationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtFilter.class, JwtUtil.class})
class ApplicationControllerTest {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final String BASE_URL         = "/applications";
    private static final String MY_APPS_URL      = "/applications/myapplications";
    private static final String DECLINE_URL      = "/applications/{id}/decline";
    private static final String PENDING_URL      = "/applications/pending";
    private static final String ASSIGNED_URL     = "/applications/assigned";
    private static final String DECISION_URL     = "/applications/{id}";
    private static final String ASSIGN_URL       = "/applications/{id}/assign";
    private static final String MEMBERS_URL      = "/applications/{id}/members";

    private static final String APPLICANT_EMAIL   = "applicant@test.com";
    private static final String UNDERWRITER_EMAIL = "underwriter@test.com";
    private static final String ADMIN_EMAIL       = "admin@test.com";

    private static final Long   APP_ID            = 1L;
    private static final Long   NON_EXISTENT_ID   = 999L;

    private static final String APP_NUMBER        = "APP-2026-0001";
    private static final String POLICY_CODE       = "HEAL_100";
    private static final double COVERAGE          = 200_000.0;

    // ── Beans ──────────────────────────────────────────────────────────────
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private UserDetailsService userDetailsService;

    // ── Fixtures ───────────────────────────────────────────────────────────
    private Application application;
    private Application declinedApplication;

    @BeforeEach
    void setUp() {
        application = new Application();
        application.setId(APP_ID);
        application.setApplicationNumber(APP_NUMBER);
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application.setRequestedCoverage(COVERAGE);
        application.setPlanType("INDIVIDUAL");
        application.setProposedPremium(10_000.0);

        declinedApplication = new Application();
        declinedApplication.setId(APP_ID);
        declinedApplication.setApplicationNumber(APP_NUMBER);
        declinedApplication.setStatus(ApplicationStatus.CUSTOMER_DECLINED);
        declinedApplication.setRequestedCoverage(COVERAGE);
        declinedApplication.setPlanType("INDIVIDUAL");
    }

    /** Builds a minimal valid single-member ApplicationRequest. */
    private ApplicationRequest buildApplicationRequest() {
        PolicyMemberRequest member = new PolicyMemberRequest();
        member.setName("John Doe");
        member.setAge(35);
        member.setBmi(24.5);
        member.setSmoker(false);
        member.setExistingDiseases("NONE");
        member.setRelationship(Relationship.SELF);

        ApplicationRequest req = new ApplicationRequest();
        req.setPolicyCode(POLICY_CODE);
        req.setRequestedCoverage(COVERAGE);
        req.setDuration(12);
        req.setMembers(List.of(member));
        return req;
    }

    /** Builds an UnderwriterDecisionRequest. */
    private UnderwriterDecisionRequest buildDecision(String status, Double premium) {
        UnderwriterDecisionRequest req = new UnderwriterDecisionRequest();
        req.setStatus(status);
        req.setFinalPremium(premium);
        return req;
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /applications
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("apply - when applicant submits valid request - should return 201 with application body")
    void apply_whenApplicantSubmitsValidRequest_shouldReturn201WithBody() throws Exception {
        when(applicationService.apply(any(ApplicationRequest.class), eq(APPLICANT_EMAIL)))
                .thenReturn(application);

        mockMvc.perform(post(BASE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildApplicationRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationNumber", is(APP_NUMBER)))
                .andExpect(jsonPath("$.planType", is("INDIVIDUAL")))
                .andExpect(jsonPath("$.requestedCoverage", is(COVERAGE)));

        verify(applicationService, times(1)).apply(any(ApplicationRequest.class), eq(APPLICANT_EMAIL));
    }

    @Test
    @DisplayName("apply - when non-applicant (underwriter) submits - should return 403 Forbidden")
    void apply_whenNonApplicantSubmits_shouldReturn403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildApplicationRequest())))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }

    @Test
    @DisplayName("apply - when request is unauthenticated - should return 401 Unauthorized")
    void apply_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildApplicationRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(applicationService);
    }

    @Test
    @DisplayName("apply - when coverage is out of policy range - should return 400 with error message")
    void apply_whenCoverageOutOfRange_shouldReturn400() throws Exception {
        when(applicationService.apply(any(ApplicationRequest.class), eq(APPLICANT_EMAIL)))
                .thenThrow(new RuntimeException("Coverage amount not in range"));

        mockMvc.perform(post(BASE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildApplicationRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Coverage amount not in range")));
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /applications/myapplications
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getMyApplications - when applicant requests own applications - should return 200 with list")
    void getMyApplications_whenApplicantRequests_shouldReturn200WithList() throws Exception {
        when(applicationService.getApplicationsByEmail(APPLICANT_EMAIL))
                .thenReturn(List.of(application));

        mockMvc.perform(get(MY_APPS_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].applicationNumber", is(APP_NUMBER)));
    }

    @Test
    @DisplayName("getMyApplications - when non-applicant requests - should return 403 Forbidden")
    void getMyApplications_whenNonApplicantRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(MY_APPS_URL)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }

    @Test
    @DisplayName("getMyApplications - when unauthenticated - should return 401 Unauthorized")
    void getMyApplications_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(MY_APPS_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(applicationService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // PATCH /applications/{id}/decline
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("declineApplication - when applicant declines own app - should return 200 with CUSTOMER_DECLINED status")
    void declineApplication_whenApplicantDeclinesOwnApp_shouldReturn200() throws Exception {
        when(applicationService.declineApplication(APP_ID, APPLICANT_EMAIL))
                .thenReturn(declinedApplication);

        mockMvc.perform(patch(DECLINE_URL, APP_ID)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CUSTOMER_DECLINED")));
    }

    @Test
    @DisplayName("declineApplication - when app does not belong to user - should return 400 with error message")
    void declineApplication_whenAppNotBelongToUser_shouldReturn400() throws Exception {
        when(applicationService.declineApplication(APP_ID, APPLICANT_EMAIL))
                .thenThrow(new RuntimeException("Unauthorized: this application does not belong to you"));

        mockMvc.perform(patch(DECLINE_URL, APP_ID)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("does not belong to you")));
    }

    @Test
    @DisplayName("declineApplication - when non-applicant requests - should return 403 Forbidden")
    void declineApplication_whenNonApplicantRequests_shouldReturn403() throws Exception {
        mockMvc.perform(patch(DECLINE_URL, APP_ID)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /applications/pending
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getPendingApplications - when underwriter requests - should return 200 with list")
    void getPendingApplications_whenUnderwriterRequests_shouldReturn200WithList() throws Exception {
        when(applicationService.getPendingApplications(UNDERWRITER_EMAIL))
                .thenReturn(List.of(application));

        mockMvc.perform(get(PENDING_URL)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].applicationNumber", is(APP_NUMBER)));
    }

    @Test
    @DisplayName("getPendingApplications - when non-underwriter requests - should return 403 Forbidden")
    void getPendingApplications_whenNonUnderwriterRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(PENDING_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /applications/assigned
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAssignedApplications - when underwriter requests - should return 200 with list")
    void getAssignedApplications_whenUnderwriterRequests_shouldReturn200WithList() throws Exception {
        when(applicationService.getAssignedApplications(UNDERWRITER_EMAIL))
                .thenReturn(List.of(application));

        mockMvc.perform(get(ASSIGNED_URL)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("getAssignedApplications - when unauthenticated - should return 401 Unauthorized")
    void getAssignedApplications_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(ASSIGNED_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(applicationService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // PATCH /applications/{id}  (underwriter decision)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateApplicationStatus - when underwriter approves application - should return 200 with updated status")
    void updateApplicationStatus_whenUnderwriterApprovesApp_shouldReturn200() throws Exception {
        Application approved = new Application();
        approved.setId(APP_ID);
        approved.setApplicationNumber(APP_NUMBER);
        approved.setStatus(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
        approved.setFinalPremium(9_500.0);

        when(applicationService.updateApplicationStatus(
                eq(APP_ID), eq("APPROVED"), eq(9_500.0), eq(UNDERWRITER_EMAIL)))
                .thenReturn(approved);

        mockMvc.perform(patch(DECISION_URL, APP_ID)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDecision("APPROVED", 9_500.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WAITING_CUSTOMER_ACCEPTANCE")))
                .andExpect(jsonPath("$.finalPremium", is(9_500.0)));

        verify(applicationService, times(1))
                .updateApplicationStatus(eq(APP_ID), eq("APPROVED"), eq(9_500.0), eq(UNDERWRITER_EMAIL));
    }

    @Test
    @DisplayName("updateApplicationStatus - when underwriter rejects application - should return 200 with REJECTED status")
    void updateApplicationStatus_whenUnderwriterRejectsApp_shouldReturn200() throws Exception {
        Application rejected = new Application();
        rejected.setId(APP_ID);
        rejected.setApplicationNumber(APP_NUMBER);
        rejected.setStatus(ApplicationStatus.REJECTED);

        when(applicationService.updateApplicationStatus(
                eq(APP_ID), eq("REJECTED"), eq(null), eq(UNDERWRITER_EMAIL)))
                .thenReturn(rejected);

        mockMvc.perform(patch(DECISION_URL, APP_ID)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDecision("REJECTED", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    @DisplayName("updateApplicationStatus - when application not assigned to this underwriter - should return 400")
    void updateApplicationStatus_whenNotAssignedUnderwriter_shouldReturn400() throws Exception {
        when(applicationService.updateApplicationStatus(
                eq(APP_ID), anyString(), any(), eq(UNDERWRITER_EMAIL)))
                .thenThrow(new RuntimeException("Unauthorized: this application is not assigned to you"));

        mockMvc.perform(patch(DECISION_URL, APP_ID)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDecision("APPROVED", 5_000.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not assigned to you")));
    }

    @Test
    @DisplayName("updateApplicationStatus - when non-underwriter requests - should return 403 Forbidden")
    void updateApplicationStatus_whenNonUnderwriterRequests_shouldReturn403() throws Exception {
        mockMvc.perform(patch(DECISION_URL, APP_ID)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDecision("APPROVED", 5_000.0))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /applications  (admin)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllApplications - when admin requests - should return 200 with full list")
    void getAllApplications_whenAdminRequests_shouldReturn200WithList() throws Exception {
        when(applicationService.getAllApplications()).thenReturn(List.of(application));

        mockMvc.perform(get(BASE_URL)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].applicationNumber", is(APP_NUMBER)));

        verify(applicationService, times(1)).getAllApplications();
    }

    @Test
    @DisplayName("getAllApplications - when non-admin requests - should return 403 Forbidden")
    void getAllApplications_whenNonAdminRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }

    @Test
    @DisplayName("getAllApplications - when unauthenticated - should return 401 Unauthorized")
    void getAllApplications_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(applicationService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // PATCH /applications/{id}/assign  (admin assigns underwriter)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("assignToUnderwriter - when admin assigns valid underwriter - should return 200 with updated application")
    void assignToUnderwriter_whenAdminAssigns_shouldReturn200() throws Exception {
        when(applicationService.assignToUnderwriter(eq(APP_ID), eq(UNDERWRITER_EMAIL), eq(ADMIN_EMAIL)))
                .thenReturn(application);

        mockMvc.perform(patch(ASSIGN_URL, APP_ID)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .param("underwriterEmail", UNDERWRITER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationNumber", is(APP_NUMBER)));

        verify(applicationService, times(1))
                .assignToUnderwriter(eq(APP_ID), eq(UNDERWRITER_EMAIL), eq(ADMIN_EMAIL));
    }

    @Test
    @DisplayName("assignToUnderwriter - when underwriter email not found - should return 400 with error message")
    void assignToUnderwriter_whenUnderwriterNotFound_shouldReturn400() throws Exception {
        when(applicationService.assignToUnderwriter(eq(APP_ID), eq("unknown@test.com"), eq(ADMIN_EMAIL)))
                .thenThrow(new RuntimeException("Underwriter not found: unknown@test.com"));

        mockMvc.perform(patch(ASSIGN_URL, APP_ID)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .param("underwriterEmail", "unknown@test.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Underwriter not found")));
    }

    @Test
    @DisplayName("assignToUnderwriter - when non-admin requests - should return 403 Forbidden")
    void assignToUnderwriter_whenNonAdminRequests_shouldReturn403() throws Exception {
        mockMvc.perform(patch(ASSIGN_URL, APP_ID)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER"))
                        .param("underwriterEmail", UNDERWRITER_EMAIL))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /applications/{id}/members  (underwriter OR admin)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getApplicationMembers - when underwriter requests - should return 200 with member list")
    void getApplicationMembers_whenUnderwriterRequests_shouldReturn200WithList() throws Exception {
        PolicyMember member = new PolicyMember();
        member.setId(1L);
        member.setName("John Doe");
        member.setAge(35);
        member.setRelationship(Relationship.SELF);

        when(applicationService.getMembersForApplication(APP_ID)).thenReturn(List.of(member));

        mockMvc.perform(get(MEMBERS_URL, APP_ID)
                        .with(user(UNDERWRITER_EMAIL).roles("UNDERWRITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[0].relationship", is("SELF")));
    }

    @Test
    @DisplayName("getApplicationMembers - when applicant requests - should return 403 Forbidden")
    void getApplicationMembers_whenApplicantRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(MEMBERS_URL, APP_ID)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(applicationService);
    }
}
