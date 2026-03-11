package org.hartford.swasthraksha.dto;

import java.util.List;

public class ApplicationRequest {
    private Double requestedCoverage;
    private Integer duration;
    private String policyCode;

    /**
     * List of plan members — must contain at least 1 entry.
     * - 1 member  → individual plan; auto-underwritten.
     * - 2+ members → family plan; sent to UNDER_REVIEW for manual assessment.
     * All member fields (name, age, bmi, smoker, existingDiseases) are required.
     */
    private List<PolicyMemberRequest> members;

    public Double getRequestedCoverage() {
        return requestedCoverage;
    }

    public void setRequestedCoverage(Double requestedCoverage) {
        this.requestedCoverage = requestedCoverage;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public List<PolicyMemberRequest> getMembers() {
        return members;
    }

    public void setMembers(List<PolicyMemberRequest> members) {
        this.members = members;
    }
}


