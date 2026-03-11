package org.hartford.swasthraksha.controller;

import org.hartford.swasthraksha.model.Users;
import org.hartford.swasthraksha.repository.UserRepository;
import org.hartford.swasthraksha.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<Users> register(@RequestBody Users u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(u));
    }
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/admin/users")
    public ResponseEntity<Users> registerUnderwriter(@RequestBody Users u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUnderwriter(u));
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/admin/users")
    public ResponseEntity<List<Users>> getUnderwriters() {
        return ResponseEntity.ok(userRepository.findByRoleContaining("UNDERWRITER"));
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/admin/claims-officers")
    public ResponseEntity<Users> registerClaimsOfficer(@RequestBody Users u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerClaimsOfficer(u));
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/admin/claims-officers")
    public ResponseEntity<List<Users>> getClaimsOfficers() {
        return ResponseEntity.ok(userRepository.findByRoleContaining("CLAIMS_OFFICER"));
    }

    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
