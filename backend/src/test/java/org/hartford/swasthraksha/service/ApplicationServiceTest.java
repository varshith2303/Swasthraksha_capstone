package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.dto.ApplicationRequest;
import org.hartford.swasthraksha.dto.PolicyMemberRequest;
import org.hartford.swasthraksha.model.*;
import org.hartford.swasthraksha.repository.ApplicationRepository;
import org.hartford.swasthraksha.repository.PolicyRepository;
import org.hartford.swasthraksha.repository.UserRepository;
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
 * Unit tests for ApplicationService.
 *
 * Layer   : Service (no Spring context)
 * Tool    : @ExtendWith(MockitoExtension.class) + @Mock + @InjectMocks
 * Naming  : methodName_whenCondition_shouldExpectedResult
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private PolicyRepository policyRepository;
    @Mock private UnderWritingService underWritingService;
    @Mock private UserRepository userRepository;

    @InjectMocks private ApplicationService applicationService;

    private Users user;
    private Policy policy;
    private Application application;

    @BeforeEach
    void setUp() {
        user = new Users();
        user.setId(1L);
        user.setEmail("applicant@test.com");
        user.setRole("APPLICANT");

        policy = new Policy();
        policy.setId(1L);
        policy.setPolicyCode("HEAL_001");
        policy.setMinCoverage(50_000.0);
        policy.setMaxCoverage(500_000.0);
        policy.setBasePercent(2.0);

        application = new Application();
        application.setId(1L);
        application.setApplicationNumber("APP-2026-0001");
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application.setRequestedCoverage(200_000.0);
        application.setUser(user);
    }

    /** Builds a complete valid SELF member request. */
    private PolicyMemberRequest buildSelfMember() {
        PolicyMemberRequest m = new PolicyMemberRequest();
        m.setName("John Doe");
        m.setAge(30);
        m.setBmi(24.0);
        m.setSmoker(false);
        m.setExistingDiseases("");
        m.setRelationship(Relationship.SELF);
        return m;
    }

    /** Builds a valid single-member ApplicationRequest. */
    private ApplicationRequest buildSingleMemberRequest() {
        ApplicationRequest req = new ApplicationRequest();
        req.setPolicyCode("HEAL_001");
        req.setRequestedCoverage(200_000.0);
        req.setDuration(3);
        req.setMembers(List.of(buildSelfMember()));
        return req;
    }

    /** Mocks applicationRepository.save() to return the same Application with id=1. */
    private void mockSaveReturnsWithId() {
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // apply — individual plan
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("apply - when individual plan with low risk - should auto-approve and set WAITING_CUSTOMER_ACCEPTANCE")
    void apply_whenIndividualLowRisk_shouldSetWaitingCustomerAcceptance() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(user);
        when(underWritingService.calculateRiskIndex(anyInt(), anyDouble(), anyBoolean(), anyString()))
                .thenReturn(0.1);
        when(underWritingService.calculateFinalPremium(anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(4_000.0);
        when(underWritingService.determineStatus(0.1)).thenReturn(ApplicationStatus.APPROVED);
        mockSaveReturnsWithId();

        Application result = applicationService.apply(buildSingleMemberRequest(), "applicant@test.com");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
        assertThat(result.getFinalPremium()).isEqualTo(4_000.0);
        assertThat(result.getPlanType()).isEqualTo("INDIVIDUAL");
        assertThat(result.getApplicationNumber()).matches("APP-\\d{4}-0001");
    }

    @Test
    @DisplayName("apply - when individual plan goes UNDER_REVIEW - should set UNDER_REVIEW status without finalPremium")
    void apply_whenIndividualMediumRisk_shouldSetUnderReviewStatus() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(user);
        when(underWritingService.calculateRiskIndex(anyInt(), anyDouble(), anyBoolean(), anyString()))
                .thenReturn(0.55);
        when(underWritingService.calculateFinalPremium(anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(6_000.0);
        when(underWritingService.determineStatus(0.55)).thenReturn(ApplicationStatus.UNDER_REVIEW);
        mockSaveReturnsWithId();

        Application result = applicationService.apply(buildSingleMemberRequest(), "applicant@test.com");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        assertThat(result.getFinalPremium()).isNull();
    }

    @Test
    @DisplayName("apply - when individual plan with high risk - should set REJECTED status")
    void apply_whenIndividualHighRisk_shouldSetRejectedStatus() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(user);
        when(underWritingService.calculateRiskIndex(anyInt(), anyDouble(), anyBoolean(), anyString()))
                .thenReturn(0.9);
        when(underWritingService.calculateFinalPremium(anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(8_000.0);
        when(underWritingService.determineStatus(0.9)).thenReturn(ApplicationStatus.REJECTED);
        mockSaveReturnsWithId();

        Application result = applicationService.apply(buildSingleMemberRequest(), "applicant@test.com");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("apply - when coverage is below policy minimum - should throw RuntimeException")
    void apply_whenCoverageBelowMinimum_shouldThrowRuntimeException() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);

        ApplicationRequest req = buildSingleMemberRequest();
        req.setRequestedCoverage(10_000.0); // below minCoverage=50_000

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> applicationService.apply(req, "applicant@test.com"));

        assertThat(ex.getMessage()).contains("not in range");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("apply - when coverage exceeds policy maximum - should throw RuntimeException")
    void apply_whenCoverageExceedsMaximum_shouldThrowRuntimeException() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);

        ApplicationRequest req = buildSingleMemberRequest();
        req.setRequestedCoverage(1_000_000.0); // above maxCoverage=500_000

        assertThrows(RuntimeException.class,
                () -> applicationService.apply(req, "applicant@test.com"));
    }

    @Test
    @DisplayName("apply - when members list is empty - should throw RuntimeException")
    void apply_whenMembersListIsEmpty_shouldThrowRuntimeException() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(user);

        ApplicationRequest req = buildSingleMemberRequest();
        req.setMembers(List.of());

        assertThrows(RuntimeException.class,
                () -> applicationService.apply(req, "applicant@test.com"));
    }

    @Test
    @DisplayName("apply - when no member has SELF relationship - should throw RuntimeException")
    void apply_whenNoSelfMember_shouldThrowRuntimeException() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(user);

        PolicyMemberRequest spouse = buildSelfMember();
        spouse.setRelationship(Relationship.SPOUSE);

        ApplicationRequest req = buildSingleMemberRequest();
        req.setMembers(List.of(spouse));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> applicationService.apply(req, "applicant@test.com"));

        assertThat(ex.getMessage()).contains("SELF");
    }

    @Test
    @DisplayName("apply - when member has missing required fields - should throw RuntimeException")
    void apply_whenMemberMissingRequiredFields_shouldThrowRuntimeException() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(user);

        PolicyMemberRequest incomplete = new PolicyMemberRequest();
        incomplete.setName(""); // blank name
        incomplete.setRelationship(Relationship.SELF);
        // age, bmi, smoker, existingDiseases are null

        ApplicationRequest req = buildSingleMemberRequest();
        req.setMembers(List.of(incomplete));

        assertThrows(RuntimeException.class,
                () -> applicationService.apply(req, "applicant@test.com"));
    }

    @Test
    @DisplayName("apply - when family plan (2+ members) - should always set UNDER_REVIEW status")
    void apply_whenFamilyPlan_shouldAlwaysSetUnderReviewStatus() {
        when(policyRepository.getByPolicyCode("HEAL_001")).thenReturn(policy);
        when(userRepository.findByEmail("applicant@test.com")).thenReturn(user);
        when(underWritingService.calculateRiskIndex(anyInt(), anyDouble(), anyBoolean(), anyString()))
                .thenReturn(0.1); // low risk — but family always goes UNDER_REVIEW
        when(underWritingService.calculateFamilyPremium(anyDouble(), anyDouble(), any(double[].class), anyInt()))
                .thenReturn(6_000.0);
        mockSaveReturnsWithId();

        PolicyMemberRequest spouse = buildSelfMember();
        spouse.setName("Jane Doe");
        spouse.setRelationship(Relationship.SPOUSE);

        ApplicationRequest req = buildSingleMemberRequest();
        req.setMembers(List.of(buildSelfMember(), spouse));

        Application result = applicationService.apply(req, "applicant@test.com");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        assertThat(result.getPlanType()).isEqualTo("FAMILY");
        verify(underWritingService).calculateFamilyPremium(anyDouble(), anyDouble(), any(double[].class), anyInt());
    }

    // ══════════════════════════════════════════════════════════════════════
    // generateApplicationNumber
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("generateApplicationNumber - when called with id 1 - should return APP-{year}-0001 format")
    void generateApplicationNumber_whenId1_shouldReturnZeroPaddedFormat() {
        String number = applicationService.generateApplicationNumber(1L);

        assertThat(number).matches("APP-\\d{4}-0001");
    }

    @Test
    @DisplayName("generateApplicationNumber - when called with id 42 - should zero-pad to 4 digits")
    void generateApplicationNumber_whenId42_shouldZeroPadTo4Digits() {
        String number = applicationService.generateApplicationNumber(42L);

        assertThat(number).matches("APP-\\d{4}-0042");
    }

    @Test
    @DisplayName("generateApplicationNumber - when called with id 10000 - should not truncate beyond 4 digits")
    void generateApplicationNumber_whenLargeId_shouldNotTruncate() {
        String number = applicationService.generateApplicationNumber(10000L);

        assertThat(number).matches("APP-\\d{4}-10000");
    }

    // ══════════════════════════════════════════════════════════════════════
    // acceptApplication
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("acceptApplication - when valid ownership and WAITING_CUSTOMER_ACCEPTANCE status - should return CUSTOMER_ACCEPTED")
    void acceptApplication_whenValid_shouldReturnCustomerAccepted() {
        application.setStatus(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
        when(applicationRepository.getByApplicationNumber("APP-2026-0001")).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(application);

        Application result = applicationService.acceptApplication("APP-2026-0001", "applicant@test.com");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.CUSTOMER_ACCEPTED);
        verify(applicationRepository).save(application);
    }

    @Test
    @DisplayName("acceptApplication - when application number not found - should throw RuntimeException")
    void acceptApplication_whenApplicationNotFound_shouldThrowRuntimeException() {
        when(applicationRepository.getByApplicationNumber("APP-XXXX")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> applicationService.acceptApplication("APP-XXXX", "applicant@test.com"));

        assertThat(ex.getMessage()).contains("not found");
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptApplication - when application belongs to a different user - should throw RuntimeException")
    void acceptApplication_whenApplicationNotOwnedByRequestingUser_shouldThrowRuntimeException() {
        application.setStatus(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
        when(applicationRepository.getByApplicationNumber("APP-2026-0001")).thenReturn(application);

        assertThrows(RuntimeException.class,
                () -> applicationService.acceptApplication("APP-2026-0001", "other@test.com"));

        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptApplication - when application status is UNDER_REVIEW - should throw RuntimeException")
    void acceptApplication_whenStatusIsNotWaitingCustomerAcceptance_shouldThrowRuntimeException() {
        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationRepository.getByApplicationNumber("APP-2026-0001")).thenReturn(application);

        assertThrows(RuntimeException.class,
                () -> applicationService.acceptApplication("APP-2026-0001", "applicant@test.com"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // declineApplication
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("declineApplication - when valid ownership and WAITING_CUSTOMER_ACCEPTANCE status - should return CUSTOMER_DECLINED")
    void declineApplication_whenValid_shouldReturnCustomerDeclined() {
        application.setStatus(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);

        Application result = applicationService.declineApplication(1L, "applicant@test.com");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.CUSTOMER_DECLINED);
    }

    @Test
    @DisplayName("declineApplication - when application not found by ID - should throw RuntimeException")
    void declineApplication_whenApplicationNotFound_shouldThrowRuntimeException() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.declineApplication(999L, "applicant@test.com"));
    }

    @Test
    @DisplayName("declineApplication - when application belongs to a different user - should throw RuntimeException")
    void declineApplication_whenApplicationNotOwnedByUser_shouldThrowRuntimeException() {
        application.setStatus(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThrows(RuntimeException.class,
                () -> applicationService.declineApplication(1L, "other@test.com"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // getPendingApplications / getAssignedApplications / getAllApplications
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getPendingApplications - when underwriter has UNDER_REVIEW apps - should return them")
    void getPendingApplications_whenUnderwriterHasPendingApps_shouldReturnThemOnly() {
        when(applicationRepository.findByAssignedToEmailAndStatus(
                "uw@test.com", ApplicationStatus.UNDER_REVIEW))
                .thenReturn(List.of(application));

        List<Application> result = applicationService.getPendingApplications("uw@test.com");

        assertThat(result).hasSize(1);
        verify(applicationRepository).findByAssignedToEmailAndStatus("uw@test.com", ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("getAssignedApplications - when underwriter has assigned apps - should return all assigned")
    void getAssignedApplications_whenAppsAssigned_shouldReturnAll() {
        when(applicationRepository.findByAssignedToEmail("uw@test.com"))
                .thenReturn(List.of(application));

        List<Application> result = applicationService.getAssignedApplications("uw@test.com");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getAllApplications - when applications exist - should return all")
    void getAllApplications_whenAppsExist_shouldReturnAll() {
        when(applicationRepository.findAll()).thenReturn(List.of(application));

        List<Application> result = applicationService.getAllApplications();

        assertThat(result).hasSize(1);
        verify(applicationRepository).findAll();
    }

    // ══════════════════════════════════════════════════════════════════════
    // assignToUnderwriter
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("assignToUnderwriter - when SUBMITTED application and valid underwriter - should assign and set UNDER_REVIEW")
    void assignToUnderwriter_whenValidSubmittedApp_shouldAssignAndSetUnderReview() {
        Users uw = new Users();
        uw.setEmail("uw@test.com");
        uw.setRole("UNDERWRITER");

        application.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findByEmail("uw@test.com")).thenReturn(uw);
        when(applicationRepository.save(application)).thenReturn(application);

        Application result = applicationService.assignToUnderwriter(1L, "uw@test.com", "admin@test.com");

        assertThat(result.getAssignedTo()).isEqualTo(uw);
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("assignToUnderwriter - when UNDER_REVIEW application - should assign without changing status")
    void assignToUnderwriter_whenUnderReviewApp_shouldAssignWithoutChangingStatus() {
        Users uw = new Users();
        uw.setEmail("uw@test.com");
        uw.setRole("UNDERWRITER");

        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findByEmail("uw@test.com")).thenReturn(uw);
        when(applicationRepository.save(application)).thenReturn(application);

        Application result = applicationService.assignToUnderwriter(1L, "uw@test.com", "admin@test.com");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    @DisplayName("assignToUnderwriter - when application is POLICY_ISSUED (finalized) - should throw RuntimeException")
    void assignToUnderwriter_whenApplicationIsFinalized_shouldThrowRuntimeException() {
        application.setStatus(ApplicationStatus.POLICY_ISSUED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThrows(RuntimeException.class,
                () -> applicationService.assignToUnderwriter(1L, "uw@test.com", "admin@test.com"));
    }

    @Test
    @DisplayName("assignToUnderwriter - when underwriter email not found - should throw RuntimeException")
    void assignToUnderwriter_whenUnderwriterNotFound_shouldThrowRuntimeException() {
        application.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findByEmail("missing@test.com")).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> applicationService.assignToUnderwriter(1L, "missing@test.com", "admin@test.com"));
    }

    @Test
    @DisplayName("assignToUnderwriter - when assigned user is not UNDERWRITER role - should throw RuntimeException")
    void assignToUnderwriter_whenUserIsNotUnderwriter_shouldThrowRuntimeException() {
        Users applicantUser = new Users();
        applicantUser.setEmail("notuw@test.com");
        applicantUser.setRole("APPLICANT");

        application.setStatus(ApplicationStatus.SUBMITTED);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findByEmail("notuw@test.com")).thenReturn(applicantUser);

        assertThrows(RuntimeException.class,
                () -> applicationService.assignToUnderwriter(1L, "notuw@test.com", "admin@test.com"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // updateApplicationStatus
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateApplicationStatus - when underwriter approves - should set WAITING_CUSTOMER_ACCEPTANCE with finalPremium")
    void updateApplicationStatus_whenApprovedByAssignedUnderwriter_shouldSetWaitingCustomerAcceptance() {
        Users uw = new Users();
        uw.setEmail("uw@test.com");

        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application.setAssignedTo(uw);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findByEmail("uw@test.com")).thenReturn(uw);
        when(applicationRepository.save(application)).thenReturn(application);

        Application result = applicationService.updateApplicationStatus(1L, "APPROVED", 5_000.0, "uw@test.com");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
        assertThat(result.getFinalPremium()).isEqualTo(5_000.0);
        assertThat(result.getDecidedBy()).isEqualTo(uw);
    }

    @Test
    @DisplayName("updateApplicationStatus - when underwriter rejects - should set REJECTED status")
    void updateApplicationStatus_whenRejectedByAssignedUnderwriter_shouldSetRejectedStatus() {
        Users uw = new Users();
        uw.setEmail("uw@test.com");

        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application.setAssignedTo(uw);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(userRepository.findByEmail("uw@test.com")).thenReturn(uw);
        when(applicationRepository.save(application)).thenReturn(application);

        Application result = applicationService.updateApplicationStatus(1L, "REJECTED", null, "uw@test.com");

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("updateApplicationStatus - when application not found - should throw RuntimeException")
    void updateApplicationStatus_whenApplicationNotFound_shouldThrowRuntimeException() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.updateApplicationStatus(999L, "APPROVED", 5_000.0, "uw@test.com"));
    }

    @Test
    @DisplayName("updateApplicationStatus - when caller is not the assigned underwriter - should throw RuntimeException")
    void updateApplicationStatus_whenCallerIsNotAssignedUnderwriter_shouldThrowRuntimeException() {
        Users differentUw = new Users();
        differentUw.setEmail("other-uw@test.com");

        application.setStatus(ApplicationStatus.UNDER_REVIEW);
        application.setAssignedTo(differentUw);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThrows(RuntimeException.class,
                () -> applicationService.updateApplicationStatus(1L, "APPROVED", 5_000.0, "uw@test.com"));
    }

    @Test
    @DisplayName("updateApplicationStatus - when application is already WAITING_CUSTOMER_ACCEPTANCE - should throw RuntimeException")
    void updateApplicationStatus_whenAlreadyWaitingCustomerAcceptance_shouldThrowRuntimeException() {
        Users uw = new Users();
        uw.setEmail("uw@test.com");

        application.setStatus(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
        application.setAssignedTo(uw);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThrows(RuntimeException.class,
                () -> applicationService.updateApplicationStatus(1L, "APPROVED", 5_000.0, "uw@test.com"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // getMembersForApplication
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getMembersForApplication - when application exists - should return list of members")
    void getMembersForApplication_whenApplicationExists_shouldReturnMembers() {
        PolicyMember member = new PolicyMember();
        member.setName("John Doe");
        member.setRelationship(Relationship.SELF);
        application.setMembers(List.of(member));

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        List<PolicyMember> result = applicationService.getMembersForApplication(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("getMembersForApplication - when application not found - should throw RuntimeException")
    void getMembersForApplication_whenApplicationNotFound_shouldThrowRuntimeException() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> applicationService.getMembersForApplication(999L));
    }

    @Test
    @DisplayName("getMembersForApplication - when application has no members - should return empty list")
    void getMembersForApplication_whenNoMembersOnApplication_shouldReturnEmptyList() {
        application.setMembers(List.of());
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        List<PolicyMember> result = applicationService.getMembersForApplication(1L);

        assertThat(result).isEmpty();
    }
}
