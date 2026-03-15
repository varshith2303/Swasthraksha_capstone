package org.hartford.swasthraksha.controller;

import org.hartford.swasthraksha.dto.ApplicationRequest;
import org.hartford.swasthraksha.dto.UnderwriterDecisionRequest;
import org.hartford.swasthraksha.model.Application;
import org.hartford.swasthraksha.model.PolicyMember;
import org.hartford.swasthraksha.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    // ── Applicant endpoints ────────────────────────────────────────────────────

    /** Applicant: submit a new insurance application */
    @PreAuthorize("hasRole('APPLICANT')")
    @PostMapping
    public ResponseEntity<Application> apply(@RequestBody ApplicationRequest a, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(a, auth.getName()));
    }

    /** Applicant: get own applications */
    @PreAuthorize("hasRole('APPLICANT')")
    @GetMapping("/myapplications")
    public ResponseEntity<List<Application>> getMyApplications(Authentication auth) {
        return ResponseEntity.ok(applicationService.getApplicationsByEmail(auth.getName()));
    }

    @PreAuthorize("hasRole('APPLICANT')")
    @PatchMapping("/{applicationNumber}/accept")
    public ResponseEntity<Application> acceptApplication(@PathVariable String applicationNumber, Authentication auth) {
        return ResponseEntity.ok(applicationService.acceptApplication(applicationNumber, auth.getName()));
    }

    @PreAuthorize("hasRole('APPLICANT')")
    @PatchMapping("/{id}/decline")
    public ResponseEntity<Application> declineApplication(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(applicationService.declineApplication(id, auth.getName()));
    }

    // ── Underwriter endpoints (assigned only) ─────────────────────────────────

    /** Underwriter: get all SUBMITTED applications (pre-assignment) */
    @PreAuthorize("hasRole('UNDERWRITER')")
    @GetMapping("/pending")
    public ResponseEntity<List<Application>> getPendingApplications(Authentication auth) {
        return ResponseEntity.ok(applicationService.getPendingApplications(auth.getName()));
    }

   
    @PreAuthorize("hasRole('UNDERWRITER')")
    @GetMapping("/assigned")
    public ResponseEntity<List<Application>> getAssignedApplications(Authentication auth) {
        return ResponseEntity.ok(applicationService.getAssignedApplications(auth.getName()));
    }

    /** Underwriter: make a decision on an application */
    @PreAuthorize("hasRole('UNDERWRITER')")
    @PatchMapping("/{id}")
    public ResponseEntity<Application> updateApplicationStatus(@PathVariable Long id,
            @RequestBody UnderwriterDecisionRequest request,
            Authentication auth) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, request.getStatus(),
                request.getFinalPremium(), auth.getName()));
    }

    
    @PreAuthorize("hasRole('admin')")
    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

   
    @PreAuthorize("hasRole('admin')")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Application> assignToUnderwriter(@PathVariable Long id,
            @RequestParam String underwriterEmail,
            Authentication auth) {
        return ResponseEntity.ok(applicationService.assignToUnderwriter(id, underwriterEmail, auth.getName()));
    }

    

   
    @PreAuthorize("hasRole('UNDERWRITER') or hasRole('admin')")
    @GetMapping("/{id}/members")
    public ResponseEntity<List<PolicyMember>> getApplicationMembers(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getMembersForApplication(id));
    }
}
//remove all comments from this file