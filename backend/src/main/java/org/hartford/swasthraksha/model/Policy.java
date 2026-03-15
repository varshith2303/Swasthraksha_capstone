package org.hartford.swasthraksha.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String policyName;
    @Column(unique = true, nullable = false)
    private String policyCode;
    private double minCoverage;
    private double maxCoverage;
    private double basePercent;
    private boolean active;
    private Integer waitingPeriodDays;

    public Integer getWaitingPeriodDays() {
        return waitingPeriodDays;
    }

    public void setWaitingPeriodDays(Integer waitingPeriodDays) {
        this.waitingPeriodDays = waitingPeriodDays;
    }

    /**
     * INDIVIDUAL, FAMILY, or BOTH (default). Controls which applicant plan types
     * can use this policy.
     */
    private String planType = "BOTH";

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }

    public double getMinCoverage() {
        return minCoverage;
    }

    public void setMinCoverage(double minCoverage) {
        this.minCoverage = minCoverage;
    }

    public double getMaxCoverage() {
        return maxCoverage;
    }

    public void setMaxCoverage(double maxCoverage) {
        this.maxCoverage = maxCoverage;
    }

    public double getBasePercent() {
        return basePercent;
    }

    public void setBasePercent(double basePercent) {
        this.basePercent = basePercent;
    }

}
