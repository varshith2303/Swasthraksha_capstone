package org.hartford.swasthraksha.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class PolicyAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String policyNumber;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    // Coverage details
    private Double coverageAmount;
    private Double remainingCoverage;

    // Premium details
    private Double premiumAmount;        // yearly premium
    private Double totalPremiumAmount;   // premium for full duration
    private Double premiumPaid;        // total premium paid so far
    // Policy duration
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationYears;

    // Claims summary
    private Double totalClaimedAmount;
    private Integer claimCount;

    // Payment configuration
    private String paymentFrequency; // ANNUAL / QUARTERLY
    private Integer totalInstallments;
    private Integer paidInstallments;

//    // Payment tracking
//    private LocalDate nextPaymentDueDate;
//    private LocalDate gracePeriodEndDate;

    // Renewal information
    private Boolean renewable;
    private LocalDate lastRenewalDate;
    private Double noClaimBonusPercentage;

    public LocalDate getLatestClaimDate() {
        return latestClaimDate;
    }

    public void setLatestClaimDate(LocalDate latestClaimDate) {
        this.latestClaimDate = latestClaimDate;
    }

    private LocalDate latestClaimDate;


    // Policy lifecycle
    @Enumerated(EnumType.STRING)
    private PolicyStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Double getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(Double coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public Double getRemainingCoverage() {
        return remainingCoverage;
    }

    public void setRemainingCoverage(Double remainingCoverage) {
        this.remainingCoverage = remainingCoverage;
    }

    public Double getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(Double premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public Double getTotalPremiumAmount() {
        return totalPremiumAmount;
    }

    public void setTotalPremiumAmount(Double totalPremiumAmount) {
        this.totalPremiumAmount = totalPremiumAmount;
    }

    public Double getPremiumPaid() {
        return premiumPaid;
    }

    public void setPremiumPaid(Double premiumPaid) {
        this.premiumPaid = premiumPaid;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getDurationYears() {
        return durationYears;
    }

    public void setDurationYears(Integer durationYears) {
        this.durationYears = durationYears;
    }

    public Double getTotalClaimedAmount() {
        return totalClaimedAmount;
    }

    public void setTotalClaimedAmount(Double totalClaimedAmount) {
        this.totalClaimedAmount = totalClaimedAmount;
    }

    public Integer getClaimCount() {
        return claimCount;
    }

    public void setClaimCount(Integer claimCount) {
        this.claimCount = claimCount;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(Integer totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public Integer getPaidInstallments() {
        return paidInstallments;
    }

    public void setPaidInstallments(Integer paidInstallments) {
        this.paidInstallments = paidInstallments;
    }

    public Boolean getRenewable() {
        return renewable;
    }

    public void setRenewable(Boolean renewable) {
        this.renewable = renewable;
    }

    public LocalDate getLastRenewalDate() {
        return lastRenewalDate;
    }

    public void setLastRenewalDate(LocalDate lastRenewalDate) {
        this.lastRenewalDate = lastRenewalDate;
    }

    public Double getNoClaimBonusPercentage() {
        return noClaimBonusPercentage;
    }

    public void setNoClaimBonusPercentage(Double noClaimBonusPercentage) {
        this.noClaimBonusPercentage = noClaimBonusPercentage;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
    }
}