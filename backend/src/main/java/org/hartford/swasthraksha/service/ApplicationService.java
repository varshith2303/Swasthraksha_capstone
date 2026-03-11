package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.dto.ApplicationRequest;
import org.hartford.swasthraksha.dto.PolicyMemberRequest;
import org.hartford.swasthraksha.model.Application;
import org.hartford.swasthraksha.model.ApplicationStatus;
import org.hartford.swasthraksha.model.Policy;
import org.hartford.swasthraksha.model.PolicyMember;
import org.hartford.swasthraksha.model.Relationship;
import org.hartford.swasthraksha.model.Users;
import org.hartford.swasthraksha.repository.ApplicationRepository;
import org.hartford.swasthraksha.repository.PolicyRepository;
import org.hartford.swasthraksha.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private UnderWritingService underWritingService;

    @Autowired
    private UserRepository userRepository;

    public Application apply(ApplicationRequest a, String email) {
        Policy p = policyRepository.getByPolicyCode(a.getPolicyCode());
        double coverageAmount = a.getRequestedCoverage();

        if (coverageAmount < p.getMinCoverage() || coverageAmount > p.getMaxCoverage()) {
            throw new RuntimeException("Coverage amount not in range");
        }

        Users u = userRepository.findByEmail(email);

        List<PolicyMemberRequest> memberRequests = a.getMembers();
        if (memberRequests == null || memberRequests.isEmpty()) {
            throw new RuntimeException("At least one member is required to submit an application");
        }

        // Exactly one member must be the primary insured (SELF)
        long selfCount = memberRequests.stream()
                .filter(m -> m.getRelationship() == Relationship.SELF)
                .count();
        if (selfCount != 1) {
            throw new RuntimeException("Exactly one member must have relationship SELF");
        }

        Application app = new Application();
        app.setRequestedCoverage(coverageAmount);
        app.setPolicy(p);
        app.setDuration(a.getDuration());
        app.setUser(u);

        if (memberRequests.size() == 1) {
            // ── INDIVIDUAL PLAN ──────────────────────────────────────────────
            app.setPlanType("INDIVIDUAL");

            PolicyMemberRequest m = memberRequests.get(0);
            validateMemberFields(m, 1);

            double risk = underWritingService.calculateRiskIndex(
                    m.getAge(), m.getBmi(), m.getSmoker(), m.getExistingDiseases());
            double proposedPremium = underWritingService.calculateFinalPremium(
                    coverageAmount, p.getBasePercent(), risk, a.getDuration());

            app.setRiskScore(risk);
            app.setProposedPremium(proposedPremium);

            ApplicationStatus status = underWritingService.determineStatus(risk);
            app.setStatus(status);
            if (status == ApplicationStatus.APPROVED) {
                app.setFinalPremium(proposedPremium);
                app.setStatus(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
            }

            app = applicationRepository.save(app);
            app.setApplicationNumber(generateApplicationNumber(app.getId()));
            app = applicationRepository.save(app);

            PolicyMember member = buildMember(m, app);
            List<PolicyMember> memberList = new ArrayList<>();
            memberList.add(member);
            app.setMembers(memberList);
            return applicationRepository.save(app);

        } else {
            // ── FAMILY PLAN (2+ members) ─────────────────────────────────────
            app.setPlanType("FAMILY");

            // Validate all members have complete fields
            for (int i = 0; i < memberRequests.size(); i++) {
                validateMemberFields(memberRequests.get(i), i + 1);
            }

            // Calculate individual risk per member
            double[] memberRisks = new double[memberRequests.size()];
            for (int i = 0; i < memberRequests.size(); i++) {
                PolicyMemberRequest m = memberRequests.get(i);
                memberRisks[i] = underWritingService.calculateRiskIndex(
                        m.getAge(), m.getBmi(), m.getSmoker(), m.getExistingDiseases());
            }

            // Single shared premium on the coverage pool using avg risk + member-count discount
            double familyPremium = underWritingService.calculateFamilyPremium(
                    coverageAmount, p.getBasePercent(), memberRisks, a.getDuration());

            double avgRisk = 0.0;
            for (double r : memberRisks) avgRisk += r;
            avgRisk = avgRisk / memberRisks.length;

            app.setRiskScore(avgRisk);
            app.setProposedPremium(familyPremium);

            // Family plans always go to UNDER_REVIEW for manual assessment
            app.setStatus(ApplicationStatus.UNDER_REVIEW);

            app = applicationRepository.save(app);
            app.setApplicationNumber(generateApplicationNumber(app.getId()));
            app = applicationRepository.save(app);

            // Persist all PolicyMember entities linked to this application
            List<PolicyMember> memberList = new ArrayList<>();
            for (PolicyMemberRequest m : memberRequests) {
                memberList.add(buildMember(m, app));
            }
            app.setMembers(memberList);
            return applicationRepository.save(app);
        }
    }

    /** Validates that all required fields of a PolicyMemberRequest are non-null. */
    private void validateMemberFields(PolicyMemberRequest m, int index) {
        List<String> missing = new ArrayList<>();
        if (m.getName() == null || m.getName().isBlank()) missing.add("name");
        if (m.getAge() == null) missing.add("age");
        if (m.getBmi() == null) missing.add("bmi");
        if (m.getSmoker() == null) missing.add("smoker");
        if (m.getExistingDiseases() == null) missing.add("existingDiseases");
        if (m.getRelationship() == null) missing.add("relationship");
        if (!missing.isEmpty()) {
            throw new RuntimeException(
                    "Member #" + index + " is missing required fields: " + String.join(", ", missing));
        }
    }

    /** Builds a PolicyMember entity from a request DTO linked to the given application. */
    private PolicyMember buildMember(PolicyMemberRequest m, Application app) {
        PolicyMember pm = new PolicyMember();
        pm.setName(m.getName());
        pm.setAge(m.getAge());
        pm.setBmi(m.getBmi());
        pm.setSmoker(m.getSmoker());
        pm.setExistingDiseases(m.getExistingDiseases());
        pm.setRelationship(m.getRelationship());
        pm.setApplication(app);
        return pm;
    }

    public String generateApplicationNumber(Long id) {
        int year = LocalDate.now().getYear();
        return "APP-" + year + "-" + String.format("%04d", id);
    }

    public List<Application> getApplicationsByStatus(String status) {
        ApplicationStatus s = ApplicationStatus.valueOf(status);
        return applicationRepository.getByStatus(s);
    }

    public List<Application> getApplicationsByEmail(String email) {
        return applicationRepository.getByUserEmail(email);
    }

    public Application declineApplication(Long id, String userEmail) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized: this application does not belong to you");
        }
        if (app.getStatus() != ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE) {
            throw new RuntimeException("Application is not awaiting your acceptance");
        }

        app.setStatus(ApplicationStatus.CUSTOMER_DECLINED);
        return applicationRepository.save(app);
    }

    // ── Underwriter: only sees apps assigned to them ──────────────────────────
    public List<Application> getPendingApplications(String underwriterEmail) {
        return applicationRepository.findByAssignedToEmailAndStatus(
                underwriterEmail, ApplicationStatus.UNDER_REVIEW);
    }

    public List<Application> getAssignedApplications(String underwriterEmail) {
        return applicationRepository.findByAssignedToEmail(underwriterEmail);
    }

    // ── Admin: full visibility ─────────────────────────────────────────────────
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Application assignToUnderwriter(Long applicationId, String underwriterEmail, String adminEmail) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.SUBMITTED &&
                app.getStatus() != ApplicationStatus.UNDER_REVIEW &&
                app.getStatus() != ApplicationStatus.DRAFT) {
            throw new RuntimeException(
                    "Cannot assign underwriter to a finalized application (Status: " + app.getStatus() + ")");
        }

        Users underwriter = userRepository.findByEmail(underwriterEmail);
        if (underwriter == null) {
            throw new RuntimeException("Underwriter not found: " + underwriterEmail);
        }
        if (!underwriter.getRole().contains("UNDERWRITER")) {
            throw new RuntimeException("User is not an underwriter");
        }

        app.setAssignedTo(underwriter);
        // Move SUBMITTED/DRAFT to UNDER_REVIEW; keep UNDER_REVIEW as-is (e.g. family plans)
        if (app.getStatus() == ApplicationStatus.SUBMITTED ||
                app.getStatus() == ApplicationStatus.DRAFT) {
            app.setStatus(ApplicationStatus.UNDER_REVIEW);
        }
        return applicationRepository.save(app);
    }

    public Application updateApplicationStatus(Long id, String status, Double finalPremium, String underwriterEmail) {
        Application app = applicationRepository.findById(id).orElse(null);
        if (app == null) {
            throw new RuntimeException("Application not found");
        }

        // Verify the underwriter is assigned to this application
        if (app.getAssignedTo() == null || !app.getAssignedTo().getEmail().equals(underwriterEmail)) {
            throw new RuntimeException("Unauthorized: this application is not assigned to you");
        }

        if (app.getStatus() == ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE) {
            throw new RuntimeException("Application is already waiting for customer acceptance");
        }
        if (app.getStatus() == ApplicationStatus.REJECTED) {
            throw new RuntimeException("Application is rejected and cannot be modified");
        }
        if (app.getStatus() == ApplicationStatus.APPROVED) {
            throw new RuntimeException("Application is already approved and cannot be modified");
        }

        ApplicationStatus requestedStatus = ApplicationStatus.valueOf(status);

        if (requestedStatus == ApplicationStatus.APPROVED) {
            app.setStatus(ApplicationStatus.WAITING_CUSTOMER_ACCEPTANCE);
            if (finalPremium != null) {
                app.setFinalPremium(finalPremium);
            }
        } else {
            app.setStatus(requestedStatus);
            if (finalPremium != null) {
                app.setFinalPremium(finalPremium);
            }
        }

        Users underwriter = userRepository.findByEmail(underwriterEmail);
        app.setDecidedBy(underwriter);

        return applicationRepository.save(app);
    }

    /**
     * Returns the list of PolicyMembers for a given application.
     * Accessible by underwriters (assigned) and admins.
     */
    public List<PolicyMember> getMembersForApplication(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        return app.getMembers();
    }
}
