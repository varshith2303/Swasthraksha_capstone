package org.hartford.swasthraksha.dto;

import org.hartford.swasthraksha.model.Relationship;

public class PolicyMemberRequest {

    private String name;
    private Integer age;
    private Double bmi;
    private Boolean smoker;
    private String existingDiseases;
    private Relationship relationship;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public Boolean getSmoker() {
        return smoker;
    }

    public void setSmoker(Boolean smoker) {
        this.smoker = smoker;
    }

    public String getExistingDiseases() {
        return existingDiseases;
    }

    public void setExistingDiseases(String existingDiseases) {
        this.existingDiseases = existingDiseases;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }
}

