package org.hartford.swasthraksha.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String applicationNumber;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private Double requestedCoverage;
    private Integer duration;

    private Double riskScore;
    private Double proposedPremium;
    private Double finalPremium;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne
    @JoinColumn(name = "decided_by")
    private Users decidedBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private Users assignedTo;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PolicyMember> members;

    private String planType; // "INDIVIDUAL" or "FAMILY"

    public List<PolicyMember> getMembers() {
        return members;
    }

    public void setMembers(List<PolicyMember> members) {
        this.members = members;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Double getRequestedCoverage() {
        return requestedCoverage;
    }

    public void setRequestedCoverage(Double requestedCoverage) {
        this.requestedCoverage = requestedCoverage;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Double getFinalPremium() {
        return finalPremium;
    }

    public void setFinalPremium(Double finalPremium) {
        this.finalPremium = finalPremium;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public Users getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(Users decidedBy) {
        this.decidedBy = decidedBy;
    }

    public Users getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Users assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Double getProposedPremium() {
        return proposedPremium;
    }

    public void setProposedPremium(Double proposedPremium) {
        this.proposedPremium = proposedPremium;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}