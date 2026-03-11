package org.hartford.swasthraksha.controller;

import org.hartford.swasthraksha.model.Policy;
import org.hartford.swasthraksha.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/policies")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    /** Admin: add a new policy */
    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public ResponseEntity<Policy> addPolicy(@RequestBody Policy p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.addPolicy(p));
    }

    /** Admin: delete a policy by id */
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok("Policy deleted successfully");
    }

    /**
     * Public: list all active policies.
     * Optional ?type=INDIVIDUAL|FAMILY|BOTH — filters by planType.
     * Policies tagged BOTH always appear in both INDIVIDUAL and FAMILY filters.
     */
    @GetMapping
    public ResponseEntity<List<Policy>> getAllPolicies(
            @RequestParam(required = false) String type) {
        List<Policy> all = policyService.getAllPolicies();
        if (type != null && !type.isBlank()) {
            String t = type.toUpperCase();
            all = all.stream()
                    .filter(p -> "BOTH".equals(p.getPlanType()) || t.equals(p.getPlanType()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(all);
    }

    /** Admin: update a policy by id */
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}")
    public ResponseEntity<Policy> updatePolicy(@PathVariable Long id, @RequestBody Policy p) {
        return ResponseEntity.ok(policyService.updatePolicy(id, p));
    }
}
