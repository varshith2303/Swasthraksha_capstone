package org.hartford.swasthraksha.controller;

import org.hartford.swasthraksha.model.PolicyAssignment;
import org.hartford.swasthraksha.service.PolicyAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/policyassignments")
public class PolicyAssignmentController {

    @Autowired
    private PolicyAssignmentService policyAssignmentService;

    @PreAuthorize("hasRole('APPLICANT')")
    @GetMapping("/my")
    public ResponseEntity<List<PolicyAssignment>> getMyPolicies(Authentication auth) {
        return ResponseEntity.ok(policyAssignmentService.getUserPolicies(auth.getName()));
    }

    @PreAuthorize("hasRole('APPLICANT')")
    @PatchMapping("/{id}/pay")
    public ResponseEntity<PolicyAssignment> payPolicy(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(policyAssignmentService.makePayment(id, auth.getName()));
    }

    @PreAuthorize("hasRole('APPLICANT')")
    @PostMapping
    public ResponseEntity<PolicyAssignment> createPolicyAssignment(@RequestBody String applicationNumber,
            Authentication auth) {
        String cleanNumber = applicationNumber.replace("\"", "");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyAssignmentService.createPolicyAssignment(cleanNumber, auth.getName()));
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/all")
    public ResponseEntity<List<PolicyAssignment>> getAllPolicies() {
        return ResponseEntity.ok(policyAssignmentService.getAllPolicies());
    }
}
