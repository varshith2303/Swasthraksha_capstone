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

    /** Admin: soft-delete a policy by toggling its active status */
    @PreAuthorize("hasRole('admin')")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Policy> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.toggleStatus(id));
    }

    /** Admin: hard-delete a policy by id */
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok("Policy deleted successfully");
    }

    /**
     * Public: list policies.
     * Optional ?type=INDIVIDUAL|FAMILY|BOTH — filters by planType.
     * Optional ?adminView=true — shows all policies including inactive (admin only).
     * By default only active policies are returned.
     */
    @GetMapping
    public ResponseEntity<List<Policy>> getAllPolicies(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "false") boolean adminView) {
        List<Policy> all = policyService.getAllPolicies();
        if (!adminView) {
            all = all.stream().filter(Policy::isActive).collect(Collectors.toList());
        }
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
