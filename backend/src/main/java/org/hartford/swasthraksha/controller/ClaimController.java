package org.hartford.swasthraksha.controller;

import org.hartford.swasthraksha.dto.ClaimRequest;
import org.hartford.swasthraksha.dto.ClaimResponse;
import org.hartford.swasthraksha.model.Claim;
import org.hartford.swasthraksha.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/claims")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PreAuthorize("hasRole('APPLICANT')")
    @PostMapping
    public ResponseEntity<ClaimResponse> submitClaim(@RequestBody ClaimRequest claimRequest,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.submitClaim(claimRequest, auth.getName()));
    }

    
    @PreAuthorize("hasRole('admin')")
    @GetMapping
    public ResponseEntity<List<Claim>> getClaims() {
        return ResponseEntity.ok(claimService.getClaims());
    }

   
    @PreAuthorize("hasRole('APPLICANT')")
    @GetMapping("/my")
    public ResponseEntity<List<ClaimResponse>> getMyClaims(Authentication auth) {
        return ResponseEntity.ok(claimService.getMyClaims(auth.getName()));
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/{claimNumber}/assign")
    public ResponseEntity<String> assignClaim(@PathVariable String claimNumber,
            @RequestParam String officerEmail) {
        claimService.assignClaim(claimNumber, officerEmail);
        return ResponseEntity.ok("Claim assigned successfully");
    }

    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @GetMapping("/assigned")
    public ResponseEntity<List<ClaimResponse>> getAssignedClaims(Authentication auth) {
        return ResponseEntity.ok(claimService.getAssignedClaims(auth.getName()));
    }

    
    @PreAuthorize("hasRole('CLAIMS_OFFICER')")
    @PostMapping("/{claimNumber}/verify")
    public ResponseEntity<String> verifyClaim(@PathVariable String claimNumber,
            @RequestParam boolean approve) {
        claimService.verifyClaim(claimNumber, approve);
        return ResponseEntity.ok("Claim verified successfully");
    }
}
