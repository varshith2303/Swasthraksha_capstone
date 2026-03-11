package org.hartford.swasthraksha.dto;

public class UnderwriterDecisionRequest {
    private String status;
    private Double finalPremium;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getFinalPremium() {
        return finalPremium;
    }

    public void setFinalPremium(Double finalPremium) {
        this.finalPremium = finalPremium;
    }
}

