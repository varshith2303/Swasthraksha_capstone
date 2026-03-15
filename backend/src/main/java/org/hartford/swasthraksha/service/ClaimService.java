package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.dto.ClaimRequest;
import org.hartford.swasthraksha.dto.ClaimResponse;
import org.hartford.swasthraksha.model.*;
import org.hartford.swasthraksha.repository.ClaimRepository;
import org.hartford.swasthraksha.repository.PolicyAssignmentRepository;
import org.hartford.swasthraksha.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyAssignmentRepository policyAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    public ClaimResponse submitClaim(ClaimRequest cr, String email) {

        PolicyAssignment p = policyAssignmentRepository.findByPolicyNumber(cr.getPolicyNumber());
        if (p == null) {
            throw new IllegalArgumentException("Policy not found: " + cr.getPolicyNumber());
        }

        Users claimant = userRepository.findByEmail(email);
        if (claimant == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }

        if (!p.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("Unauthorized: this policy does not belong to you.");
        }

        // Only ACTIVE policies can have claims filed against them
        if (p.getStatus() != PolicyStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Claims can only be filed on ACTIVE policies. Current status: " + p.getStatus());
        }

        // Validate member if provided
        PolicyMember patient = null;
        if (cr.getMemberId() != null) {
            patient = p.getApplication().getMembers().stream()
                    .filter(m -> m.getId().equals(cr.getMemberId()))
                    .findFirst()
                    .orElseThrow(
                            () -> new IllegalArgumentException("Member not found in this policy: " + cr.getMemberId()));
        }

        // 30-day waiting period: baseline is the last claim date, or policy start date
        LocalDate baseline = claimRepository.findLatestClaimDateByPolicy(p)
                .map(LocalDateTime::toLocalDate)
                .orElse(p.getStartDate());
        Integer days=p.getApplication().getPolicy().getWaitingPeriodDays();

        if (LocalDate.now().isBefore(baseline.plusDays(days))) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), baseline.plusDays(days));
            throw new IllegalStateException(
                    "Waiting period not met. You can file a new claim in " + daysLeft + " day(s).");
        }

        // Validate claim amount does not exceed remaining coverage
        double remaining = p.getRemainingCoverage() != null ? p.getRemainingCoverage() : p.getCoverageAmount();
        if (cr.getClaimAmount() <= 0) {
            throw new IllegalStateException("Claim amount must be greater than zero.");
        }
        if (cr.getClaimAmount() > remaining) {
            throw new IllegalStateException(
                    "Claim amount (₹" + cr.getClaimAmount() + ") exceeds remaining coverage (₹" + remaining + ").");
        }

        Claim c = new Claim();
        c.setClaimNumber("CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        c.setPolicy(p);
        c.setClaimant(claimant);
        c.setPatient(patient);
        c.setClaimAmount(cr.getClaimAmount());
        c.setHospitalName(cr.getHospitalName());
        c.setClaimReason(cr.getClaimReason());
        c.setAdmissionDate(cr.getAdmissionDate());
        c.setDischargeDate(cr.getDischargeDate());
        c.setStatus(ClaimStatus.PENDING);
        c.setApprovedAmount(0.0);
        c.setCreatedAt(LocalDateTime.now());

        Claim saved = claimRepository.save(c);
        return mapToResponse(saved);
    }

    private ClaimResponse mapToResponse(Claim c) {
        ClaimResponse response = new ClaimResponse();
        response.setClaimNumber(c.getClaimNumber());
        response.setClaimAmount(c.getClaimAmount());
        response.setApprovedAmount(c.getApprovedAmount());
        response.setHospitalName(c.getHospitalName());
        response.setStatus(c.getStatus());
        response.setAdmissionDate(c.getAdmissionDate());
        response.setDischargeDate(c.getDischargeDate());
        response.setClaimReason(c.getClaimReason());
        response.setReviewedDate(c.getReviewDate());
        if (c.getPolicy() != null) {
            response.setPolicyNumber(c.getPolicy().getPolicyNumber());
            if (c.getPolicy().getApplication() != null && c.getPolicy().getApplication().getPolicy() != null) {
                response.setPolicyName(c.getPolicy().getApplication().getPolicy().getPolicyName());
            }
        }
        if (c.getPatient() != null) {
            response.setPatientName(c.getPatient().getName());
        }
        if (c.getCreatedAt() != null) {
            response.setSubmittedDate(c.getCreatedAt().toLocalDate());
        }
        return response;
    }

    public List<Claim> getClaims() {
        return claimRepository.findAll();
    }

    public List<ClaimResponse> getMyClaims(String email) {
        Users claimant = userRepository.findByEmail(email);
        if (claimant == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        return claimRepository.findByClaimant(claimant).stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public void assignClaim(String claimNumber, String officerEmail) {
        Claim claim = claimRepository.findByClaimNumber(claimNumber);
        if (claim == null) {
            throw new IllegalArgumentException("Claim not found: " + claimNumber);
        }

        Users officer = userRepository.findByEmail(officerEmail);
        if (officer == null || !"CLAIMS_OFFICER".equals(officer.getRole())) {
            throw new IllegalArgumentException("Claims Officer not found: " + officerEmail);
        }

        claim.setReviewedBy(officer);
        claimRepository.save(claim);
    }

    public List<ClaimResponse> getAssignedClaims(String email) {
        Users officer = userRepository.findByEmail(email);
        if (officer == null) {
            throw new IllegalArgumentException("Claims officer not found: " + email);
        }
        return claimRepository.findByReviewedBy(officer).stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public void verifyClaim(String claimNumber, boolean approve) {
        Claim claim = claimRepository.findByClaimNumber(claimNumber);
        if (claim == null) {
            throw new IllegalArgumentException("Claim not found: " + claimNumber);
        }

        claim.setReviewDate(LocalDate.now());

        if (approve) {
            PolicyAssignment policy = claim.getPolicy();
            double remaining = policy.getRemainingCoverage() != null
                    ? policy.getRemainingCoverage()
                    : policy.getCoverageAmount();
            double approvedAmt = Math.min(claim.getClaimAmount(), remaining);

            claim.setApprovedAmount(approvedAmt);
            claim.setStatus(ClaimStatus.APPROVED);
            LocalDate baseline = claimRepository.findLatestClaimDateByPolicy(policy)
                    .map(LocalDateTime::toLocalDate)
                    .orElse(policy.getStartDate());
            policy.setLatestClaimDate(LocalDate.now());
            policy.setRemainingCoverage(remaining - approvedAmt);
            policy.setTotalClaimedAmount(
                    (policy.getTotalClaimedAmount() != null ? policy.getTotalClaimedAmount() : 0.0) + approvedAmt);
            policy.setClaimCount(
                    (policy.getClaimCount() != null ? policy.getClaimCount() : 0) + 1);
            policyAssignmentRepository.save(policy);
        } else {
            claim.setStatus(ClaimStatus.REJECTED);
        }

        claimRepository.save(claim);
    }
}
