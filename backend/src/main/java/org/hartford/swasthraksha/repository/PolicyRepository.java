package org.hartford.swasthraksha.repository;

import org.hartford.swasthraksha.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    public Policy getByPolicyCode(String policyCode);

    public Policy getById(Long id);

}
