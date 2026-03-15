package org.hartford.swasthraksha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.swasthraksha.config.SecurityConfig;
import org.hartford.swasthraksha.dto.ClaimRequest;
import org.hartford.swasthraksha.dto.ClaimResponse;
import org.hartford.swasthraksha.exception.GlobalExceptionHandler;
import org.hartford.swasthraksha.filter.JwtFilter;
import org.hartford.swasthraksha.model.Claim;
import org.hartford.swasthraksha.model.ClaimStatus;
import org.hartford.swasthraksha.service.ClaimService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ClaimController.
 *
 * Layer    : Controller (Web Layer only — no DB, no full Spring context)
 * Tool     : @WebMvcTest + MockMvc
 * Mocking  : @MockBean replaces ClaimService
 * Auth     : SecurityMockMvcRequestPostProcessors.user() simulates JWT roles
 * Naming   : methodName_whenCondition_shouldExpectedResult
 */
@WebMvcTest(ClaimController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtFilter.class, JwtUtil.class})
class ClaimControllerTest {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final String BASE_URL      = "/claims";
    private static final String MY_CLAIMS_URL = "/claims/my";
    private static final String ASSIGN_URL    = "/claims/{claimNumber}/assign";
    private static final String ASSIGNED_URL  = "/claims/assigned";
    private static final String VERIFY_URL    = "/claims/{claimNumber}/verify";

    private static final String APPLICANT_EMAIL = "applicant@test.com";
    private static final String ADMIN_EMAIL     = "admin@test.com";
    private static final String OFFICER_EMAIL   = "officer@test.com";

    private static final String CLAIM_NUMBER    = "CLM-2026-0001";
    private static final String POLICY_NUMBER   = "POL-2026-0001";
    private static final double CLAIM_AMOUNT    = 50_000.0;

    // ── Beans ──────────────────────────────────────────────────────────────
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    @MockBean
    private UserDetailsService userDetailsService;

    // ── Fixtures ───────────────────────────────────────────────────────────
    private ClaimResponse claimResponse;
    private Claim claim;

    @BeforeEach
    void setUp() {
        claimResponse = new ClaimResponse();
        claimResponse.setClaimNumber(CLAIM_NUMBER);
        claimResponse.setClaimAmount(CLAIM_AMOUNT);
        claimResponse.setHospitalName("City Hospital");
        claimResponse.setStatus(ClaimStatus.PENDING);
        claimResponse.setPolicyNumber(POLICY_NUMBER);
        claimResponse.setSubmittedDate(LocalDate.of(2026, 3, 12));

        claim = new Claim();
        claim.setClaimNumber(CLAIM_NUMBER);
        claim.setClaimAmount(CLAIM_AMOUNT);
        claim.setHospitalName("City Hospital");
        claim.setStatus(ClaimStatus.PENDING);
    }

    /** Builds a minimal valid ClaimRequest. */
    private ClaimRequest buildClaimRequest() {
        ClaimRequest req = new ClaimRequest();
        req.setPolicyNumber(POLICY_NUMBER);
        req.setClaimAmount(CLAIM_AMOUNT);
        req.setHospitalName("City Hospital");
        req.setClaimReason("Surgery");
        req.setAdmissionDate(LocalDate.of(2026, 3, 1));
        req.setDischargeDate(LocalDate.of(2026, 3, 5));
        req.setMemberId(1L);
        return req;
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /claims
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("submitClaim - when applicant submits valid claim - should return 201 with claim body")
    void submitClaim_whenApplicantSubmitsValidClaim_shouldReturn201WithBody() throws Exception {
        when(claimService.submitClaim(any(ClaimRequest.class), eq(APPLICANT_EMAIL)))
                .thenReturn(claimResponse);

        mockMvc.perform(post(BASE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildClaimRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimNumber", is(CLAIM_NUMBER)))
                .andExpect(jsonPath("$.claimAmount", is(CLAIM_AMOUNT)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(claimService, times(1)).submitClaim(any(ClaimRequest.class), eq(APPLICANT_EMAIL));
    }

    @Test
    @DisplayName("submitClaim - when non-applicant submits - should return 403 Forbidden")
    void submitClaim_whenNonApplicantSubmits_shouldReturn403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(user(OFFICER_EMAIL).roles("CLAIMS_OFFICER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildClaimRequest())))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claimService);
    }

    @Test
    @DisplayName("submitClaim - when unauthenticated - should return 401 Unauthorized")
    void submitClaim_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildClaimRequest())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(claimService);
    }

    @Test
    @DisplayName("submitClaim - when policy number is not found - should return 400 with error message")
    void submitClaim_whenPolicyNotFound_shouldReturn400() throws Exception {
        when(claimService.submitClaim(any(ClaimRequest.class), eq(APPLICANT_EMAIL)))
                .thenThrow(new RuntimeException("Policy assignment not found: " + POLICY_NUMBER));

        mockMvc.perform(post(BASE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildClaimRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(POLICY_NUMBER)));
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /claims  (admin: all claims)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getClaims - when admin requests all claims - should return 200 with list")
    void getClaims_whenAdminRequests_shouldReturn200WithList() throws Exception {
        when(claimService.getClaims()).thenReturn(List.of(claim));

        mockMvc.perform(get(BASE_URL)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].claimNumber", is(CLAIM_NUMBER)));

        verify(claimService, times(1)).getClaims();
    }

    @Test
    @DisplayName("getClaims - when non-admin requests - should return 403 Forbidden")
    void getClaims_whenNonAdminRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claimService);
    }

    @Test
    @DisplayName("getClaims - when unauthenticated - should return 401 Unauthorized")
    void getClaims_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(claimService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /claims/my
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getMyClaims - when applicant requests own claims - should return 200 with list")
    void getMyClaims_whenApplicantRequests_shouldReturn200WithList() throws Exception {
        when(claimService.getMyClaims(APPLICANT_EMAIL)).thenReturn(List.of(claimResponse));

        mockMvc.perform(get(MY_CLAIMS_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].claimNumber", is(CLAIM_NUMBER)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test
    @DisplayName("getMyClaims - when non-applicant requests - should return 403 Forbidden")
    void getMyClaims_whenNonApplicantRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(MY_CLAIMS_URL)
                        .with(user(OFFICER_EMAIL).roles("CLAIMS_OFFICER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claimService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /claims/{claimNumber}/assign  (admin assigns claims officer)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("assignClaim - when admin assigns valid claims officer - should return 200 with success message")
    void assignClaim_whenAdminAssigns_shouldReturn200WithSuccessMessage() throws Exception {
        doNothing().when(claimService).assignClaim(eq(CLAIM_NUMBER), eq(OFFICER_EMAIL));

        mockMvc.perform(post(ASSIGN_URL, CLAIM_NUMBER)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .param("officerEmail", OFFICER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(content().string("Claim assigned successfully"));

        verify(claimService, times(1)).assignClaim(eq(CLAIM_NUMBER), eq(OFFICER_EMAIL));
    }

    @Test
    @DisplayName("assignClaim - when claims officer email not found - should return 400 with error message")
    void assignClaim_whenOfficerNotFound_shouldReturn400() throws Exception {
        doThrow(new RuntimeException("Claims officer not found: unknown@test.com"))
                .when(claimService).assignClaim(eq(CLAIM_NUMBER), eq("unknown@test.com"));

        mockMvc.perform(post(ASSIGN_URL, CLAIM_NUMBER)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .param("officerEmail", "unknown@test.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Claims officer not found")));
    }

    @Test
    @DisplayName("assignClaim - when non-admin requests - should return 403 Forbidden")
    void assignClaim_whenNonAdminRequests_shouldReturn403() throws Exception {
        mockMvc.perform(post(ASSIGN_URL, CLAIM_NUMBER)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .param("officerEmail", OFFICER_EMAIL))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claimService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /claims/assigned  (claims officer: own assigned claims)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAssignedClaims - when claims officer requests - should return 200 with list")
    void getAssignedClaims_whenOfficerRequests_shouldReturn200WithList() throws Exception {
        when(claimService.getAssignedClaims(OFFICER_EMAIL)).thenReturn(List.of(claimResponse));

        mockMvc.perform(get(ASSIGNED_URL)
                        .with(user(OFFICER_EMAIL).roles("CLAIMS_OFFICER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].claimNumber", is(CLAIM_NUMBER)));
    }

    @Test
    @DisplayName("getAssignedClaims - when non-officer requests - should return 403 Forbidden")
    void getAssignedClaims_whenNonOfficerRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(ASSIGNED_URL)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claimService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /claims/{claimNumber}/verify  (claims officer approves/rejects)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("verifyClaim - when officer approves claim - should return 200 with success message")
    void verifyClaim_whenOfficerApproves_shouldReturn200WithMessage() throws Exception {
        doNothing().when(claimService).verifyClaim(eq(CLAIM_NUMBER), eq(true));

        mockMvc.perform(post(VERIFY_URL, CLAIM_NUMBER)
                        .with(user(OFFICER_EMAIL).roles("CLAIMS_OFFICER"))
                        .param("approve", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("Claim verified successfully"));

        verify(claimService, times(1)).verifyClaim(eq(CLAIM_NUMBER), eq(true));
    }

    @Test
    @DisplayName("verifyClaim - when officer rejects claim - should return 200 with success message")
    void verifyClaim_whenOfficerRejects_shouldReturn200WithMessage() throws Exception {
        doNothing().when(claimService).verifyClaim(eq(CLAIM_NUMBER), eq(false));

        mockMvc.perform(post(VERIFY_URL, CLAIM_NUMBER)
                        .with(user(OFFICER_EMAIL).roles("CLAIMS_OFFICER"))
                        .param("approve", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("Claim verified successfully"));

        verify(claimService, times(1)).verifyClaim(eq(CLAIM_NUMBER), eq(false));
    }

    @Test
    @DisplayName("verifyClaim - when non-officer requests - should return 403 Forbidden")
    void verifyClaim_whenNonOfficerRequests_shouldReturn403() throws Exception {
        mockMvc.perform(post(VERIFY_URL, CLAIM_NUMBER)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .param("approve", "true"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(claimService);
    }
}