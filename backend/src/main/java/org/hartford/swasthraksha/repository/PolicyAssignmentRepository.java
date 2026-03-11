package org.hartford.swasthraksha.repository;

import org.hartford.swasthraksha.model.PolicyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface PolicyAssignmentRepository extends JpaRepository<PolicyAssignment, Long> {

    List<PolicyAssignment> findAll();

    public PolicyAssignment findByPolicyNumber(String policyNumber);


    List<PolicyAssignment> findByUserEmail(String email);
}
