package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.model.*;
import org.hartford.swasthraksha.repository.ApplicationRepository;
import org.hartford.swasthraksha.repository.PolicyAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PolicyAssignmentService.
 *
 * Layer   : Service (no Spring context)
 * Tool    : @ExtendWith(MockitoExtension.class) + @Mock + @InjectMocks
 * Naming  : methodName_whenCondition_shouldExpectedResult
 */
@ExtendWith(MockitoExtension.class)
class PolicyAssignmentServiceTest {

    @Mock private PolicyAssignmentRepository pa;
    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks private PolicyAssignmentService policyAssignmentService;

    private Users user;
    private Application application;
    private PolicyAssignment assignment;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setId(1L);
        user.setEmail("applicant@test.com");
        user.setRole("APPLICANT");

        application = new Application();
        application.setId(1L);
        application.setApplicationNumber("APP-2026-0001");
        application.setStatus(ApplicationStatus.CUSTOMER_ACCEPTED);
        application.setRequestedCoverage(200_000.0);
        application.setFinalPremium(5_000.0);
        application.setDuration(3);
        application.setUser(user);

        assignment = new PolicyAssignment();
        assignment.setId(1L);
        assignment.setUser(user);
        assignment.setPremiumAmount(5_000.0);
        assignment.setStatus(PolicyStatus.PENDING_PAYMENT);
        assignment.setDurationYears(3);
    }

    // ══════════════════════════════════════════════════════════════════════
    // getUserPolicies
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getUserPolicies - when user has policies - should return the user's policies list")
    void getUserPolicies_whenUserHasPolicies_shouldReturnList() {
        when(pa.findByUserEmail("applicant@test.com")).thenReturn(List.of(assignment));

        List<PolicyAssignment> result = policyAssignmentService.getUserPolicies("applicant@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(pa).findByUserEmail("applicant@test.com");
    }

    @Test
    @DisplayName("getUserPolicies - when user has no policies - should return empty list")
    void getUserPolicies_whenUserHasNoPolicies_shouldReturnEmptyList() {
        when(pa.findByUserEmail("new@test.com")).thenReturn(List.of());

        List<PolicyAssignment> result = policyAssignmentService.getUserPolicies("new@test.com");

        assertThat(result).isEmpty();
    }

    // ══════════════════════════════════════════════════════════════════════
    // getAllPolicies
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllPolicies - when assignments exist - should return all assignments")
    void getAllPolicies_whenAssignmentsExist_shouldReturnAll() {
        when(pa.findAll()).thenReturn(List.of(assignment));

        List<PolicyAssignment> result = policyAssignmentService.getAllPolicies();

        assertThat(result).hasSize(1);
        verify(pa).findAll();
    }

    @Test
    @DisplayName("getAllPolicies - when no assignments exist - should return empty list")
    void getAllPolicies_whenNoAssignments_shouldReturnEmptyList() {
        when(pa.findAll()).thenReturn(List.of());

        assertThat(policyAssignmentService.getAllPolicies()).isEmpty();
    }

    // ══════════════════════════════════════════════════════════════════════
    // makePayment
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("makePayment - when valid PENDING_PAYMENT policy - should activate, set premium paid, and set dates")
    void makePayment_whenValidPendingPaymentPolicy_shouldActivatePolicyAndSetDates() {
        when(pa.findById(1L)).thenReturn(Optional.of(assignment));
        when(pa.save(assignment)).thenReturn(assignment);

        PolicyAssignment result = policyAssignmentService.makePayment(1L, "applicant@test.com");

        assertThat(result.getStatus()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(result.getPremiumPaid()).isEqualTo(5_000.0);
        assertThat(result.getPaidInstallments()).isEqualTo(1);
        assertThat(result.getStartDate()).isNotNull();
        assertThat(result.getEndDate()).isNotNull();
        assertThat(result.getEndDate()).isAfter(result.getStartDate());
        verify(pa).save(assignment);
    }

    @Test
    @DisplayName("makePayment - when policy not found - should throw RuntimeException")
    void makePayment_whenPolicyNotFound_shouldThrowRuntimeException() {
        when(pa.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> policyAssignmentService.makePayment(999L, "applicant@test.com"));

        assertThat(ex.getMessage()).contains("not found");
        verify(pa, never()).save(any());
    }

    @Test
    @DisplayName("makePayment - when policy belongs to a different user - should throw RuntimeException")
    void makePayment_whenPolicyBelongsToAnotherUser_shouldThrowRuntimeException() {
        when(pa.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(RuntimeException.class,
                () -> policyAssignmentService.makePayment(1L, "intruder@test.com"));

        verify(pa, never()).save(any());
    }

    @Test
    @DisplayName("makePayment - when policy status is not PENDING_PAYMENT - should throw RuntimeException")
    void makePayment_whenPolicyNotInPendingPaymentStatus_shouldThrowRuntimeException() {
        assignment.setStatus(PolicyStatus.ACTIVE);
        when(pa.findById(1L)).thenReturn(Optional.of(assignment));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> policyAssignmentService.makePayment(1L, "applicant@test.com"));

        assertThat(ex.getMessage()).contains("PENDING_PAYMENT");
        verify(pa, never()).save(any());
    }

    @Test
    @DisplayName("makePayment - when policy is EXPIRED - should throw RuntimeException")
    void makePayment_whenPolicyIsExpired_shouldThrowRuntimeException() {
        assignment.setStatus(PolicyStatus.EXPIRED);
        when(pa.findById(1L)).thenReturn(Optional.of(assignment));

        assertThrows(RuntimeException.class,
                () -> policyAssignmentService.makePayment(1L, "applicant@test.com"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // createPolicyAssignment
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createPolicyAssignment - when CUSTOMER_ACCEPTED application - should create active assignment and mark app POLICY_ISSUED")
    void createPolicyAssignment_whenValidCustomerAcceptedApplication_shouldCreateActiveAssignment() {
        when(applicationRepository.getByApplicationNumber("APP-2026-0001")).thenReturn(application);
        when(pa.save(any(PolicyAssignment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.save(application)).thenReturn(application);

        PolicyAssignment result = policyAssignmentService
                .createPolicyAssignment("APP-2026-0001", "applicant@test.com");

        assertThat(result.getStatus()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(result.getCoverageAmount()).isEqualTo(200_000.0);
        assertThat(result.getPremiumAmount()).isEqualTo(5_000.0);
        assertThat(result.getRemainingCoverage()).isEqualTo(200_000.0);
        assertThat(result.getDurationYears()).isEqualTo(3);
        assertThat(result.getPolicyNumber()).startsWith("POL-");
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.POLICY_ISSUED);
        verify(pa).save(any(PolicyAssignment.class));
        verify(applicationRepository).save(application);
    }

    @Test
    @DisplayName("createPolicyAssignment - when application number not found - should throw RuntimeException")
    void createPolicyAssignment_whenApplicationNotFound_shouldThrowRuntimeException() {
        when(applicationRepository.getByApplicationNumber("APP-XXXX")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> policyAssignmentService.createPolicyAssignment("APP-XXXX", "applicant@test.com"));

        assertThat(ex.getMessage()).contains("APP-XXXX");
        verify(pa, never()).save(any());
    }

    @Test
    @DisplayName("createPolicyAssignment - when application does not belong to user - should throw RuntimeException")
    void createPolicyAssignment_whenUnauthorizedUser_shouldThrowRuntimeException() {
        when(applicationRepository.getByApplicationNumber("APP-2026-0001")).thenReturn(application);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> policyAssignmentService
                        .createPolicyAssignment("APP-2026-0001", "hacker@test.com"));

        assertThat(ex.getMessage()).contains("Unauthorized");
        verify(pa, never()).save(any());
    }

    @Test
    @DisplayName("createPolicyAssignment - when application status is UNDER_REVIEW - should throw RuntimeException")
    void createPolicyAssignment_whenApplicationStatusIsUnderReview_shouldThrowRuntimeException() {
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationRepository.getByApplicationNumber("APP-2026-0001")).thenReturn(application);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> policyAssignmentService
                        .createPolicyAssignment("APP-2026-0001", "applicant@test.com"));

        assertThat(ex.getMessage()).contains("CUSTOMER_ACCEPTED");
        verify(pa, never()).save(any());
    }

    @Test
    @DisplayName("createPolicyAssignment - when application status is CUSTOMER_DECLINED - should throw RuntimeException")
    void createPolicyAssignment_whenApplicationDeclined_shouldThrowRuntimeException() {
        application.setStatus(ApplicationStatus.CUSTOMER_DECLINED);
        when(applicationRepository.getByApplicationNumber("APP-2026-0001")).thenReturn(application);

        assertThrows(RuntimeException.class,
                () -> policyAssignmentService
                        .createPolicyAssignment("APP-2026-0001", "applicant@test.com"));
    }
}
