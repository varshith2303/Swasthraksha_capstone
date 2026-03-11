package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.model.Policy;
import org.hartford.swasthraksha.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyService {
    @Autowired
    private PolicyRepository policyRepository;

    public Policy addPolicy(Policy p) {
        if (policyRepository.getByPolicyCode(p.getPolicyCode()) != null) {
            throw new RuntimeException("Policy with code " + p.getPolicyCode() + " already exists");
        }
        return policyRepository.save(p);
    }

    public void deletePolicy(Long id) {
        Policy policy = policyRepository.findById(id).orElse(null);
        if (policy != null) {
            policyRepository.delete(policy);
            return;
        }
        throw new RuntimeException("Policy not found");

    }

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public Policy updatePolicy(Long id, Policy p) {
        Policy existing = policyRepository.findById(id).orElse(null);
        if (existing != null) {
            // Check if the new code already exists in another policy
            Policy withSameCode = policyRepository.getByPolicyCode(p.getPolicyCode());
            if (withSameCode != null && !withSameCode.getId().equals(id)) {
                throw new RuntimeException("Another policy with code " + p.getPolicyCode() + " already exists");
            }
            p.setId(id);
            return policyRepository.save(p);
        }
        throw new RuntimeException("Policy not found");
    }

}
