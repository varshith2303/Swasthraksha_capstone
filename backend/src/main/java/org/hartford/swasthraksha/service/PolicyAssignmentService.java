package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.model.Application;
import org.hartford.swasthraksha.model.PolicyAssignment;
import org.hartford.swasthraksha.model.PolicyStatus;
import org.hartford.swasthraksha.repository.ApplicationRepository;
import org.hartford.swasthraksha.repository.PolicyAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PolicyAssignmentService {
    @Autowired
    private PolicyAssignmentRepository pa;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<PolicyAssignment> getUserPolicies(String email) {
        return pa.findByUserEmail(email);
    }

    public List<PolicyAssignment> getAllPolicies() {
        return pa.findAll();
    }

    @Transactional
    public PolicyAssignment makePayment(Long policyId, String userEmail) {
        PolicyAssignment assignment = pa.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        if (!assignment.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: policy does not belong to user");
        }

        if (assignment.getStatus() != PolicyStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Policy is not in PENDING_PAYMENT status");
        }

        // Mock payment processing — in real app, integrate payment gateway
        assignment.setPremiumPaid(assignment.getPremiumAmount());
        assignment.setPaidInstallments(1);
        assignment.setStatus(PolicyStatus.ACTIVE);

        // Finalize dates only after payment
        LocalDate startDate = LocalDate.now();
        assignment.setStartDate(startDate);
        assignment.setEndDate(startDate.plusYears(assignment.getDurationYears()));

        return pa.save(assignment);
    }

    @Transactional
    public PolicyAssignment createPolicyAssignment(String applicationNumber, String userEmail) {
        Application app = applicationRepository.getByApplicationNumber(applicationNumber);

        if (app == null) {
            throw new RuntimeException("Application not found: " + applicationNumber);
        }

        if (!app.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: Application does not belong to user");
        }

        if (app.getStatus() != org.hartford.swasthraksha.model.ApplicationStatus.CUSTOMER_ACCEPTED) {
            throw new RuntimeException("Application must be in CUSTOMER_ACCEPTED status to make first payment");
        }

        PolicyAssignment assignment = new PolicyAssignment();

        // Generate a unique policy number
        assignment.setPolicyNumber("POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Link user and application
        assignment.setUser(app.getUser());
        assignment.setApplication(app);

        // Coverage details from the approved application
        assignment.setCoverageAmount(app.getRequestedCoverage());
        assignment.setRemainingCoverage(app.getRequestedCoverage());

        // Premium details
        Double yearlyPremium = app.getFinalPremium();
        Integer duration = app.getDuration();
        assignment.setPremiumAmount(yearlyPremium);
        assignment.setTotalPremiumAmount(yearlyPremium * duration);
        assignment.setPremiumPaid(0.0);

        // Policy duration
        // LocalDate startDate = LocalDate.now();
        // assignment.setStartDate(startDate);
        // assignment.setEndDate(startDate.plusYears(duration));
        assignment.setDurationYears(duration);

        // Claims summary — nothing claimed yet
        assignment.setTotalClaimedAmount(0.0);
        assignment.setClaimCount(0);

        // Payment configuration (default: annual)
        assignment.setPaymentFrequency("ANNUAL");
        assignment.setTotalInstallments(duration);
        assignment.setPaidInstallments(0);

        // Renewal information
        assignment.setRenewable(true);
        assignment.setLastRenewalDate(null);
        assignment.setNoClaimBonusPercentage(0.0);

        // Activate policy immediately upon first payment
        assignment.setStatus(PolicyStatus.ACTIVE);
        assignment.setPremiumPaid(yearlyPremium);
        assignment.setPaidInstallments(1);
        LocalDate startDate = LocalDate.now();
        assignment.setStartDate(startDate);
        assignment.setEndDate(startDate.plusYears(duration));

        PolicyAssignment saved = pa.save(assignment);

        // Update the application status to POLICY_ISSUED
        app.setStatus(org.hartford.swasthraksha.model.ApplicationStatus.POLICY_ISSUED);
        applicationRepository.save(app);

        return saved;
    }
}
