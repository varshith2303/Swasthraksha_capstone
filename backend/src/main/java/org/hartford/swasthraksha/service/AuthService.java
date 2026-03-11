package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.model.Application;
import org.hartford.swasthraksha.model.Claim;
import org.hartford.swasthraksha.model.Users;
import org.hartford.swasthraksha.repository.ApplicationRepository;
import org.hartford.swasthraksha.repository.ClaimRepository;
import org.hartford.swasthraksha.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private PasswordEncoder pe;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ApplicationRepository applicationRepo;

    @Autowired
    private ClaimRepository claimRepo;

    public Users register(Users u) {
        if (userRepo.findByEmail(u.getEmail()) != null) {
            throw new RuntimeException("This email is already registered: " + u.getEmail());
        }
        u.setRole("APPLICANT");
        u.setPassword(pe.encode(u.getPassword()));
        return userRepo.save(u);
    }

    public Users registerUnderwriter(Users u) {
        if (userRepo.findByEmail(u.getEmail()) != null) {
            throw new RuntimeException("This email is already registered: " + u.getEmail());
        }
        u.setRole("UNDERWRITER");
        u.setPassword(pe.encode(u.getPassword()));
        return userRepo.save(u);
    }

    public Users registerClaimsOfficer(Users u) {
        if (userRepo.findByEmail(u.getEmail()) != null) {
            throw new RuntimeException("This email is already registered: " + u.getEmail());
        }
        u.setRole("CLAIMS_OFFICER");
        u.setPassword(pe.encode(u.getPassword()));
        return userRepo.save(u);
    }

    @Transactional
    public void deleteUser(Long id) {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        String role = user.getRole();

        // If deleting an underwriter, check for assigned applications
        if ("UNDERWRITER".equals(role)) {
            List<Application> assigned = applicationRepo.findByAssignedToEmail(user.getEmail());
            if (!assigned.isEmpty()) {
                // Null out the assignments so the delete can proceed
                for (Application app : assigned) {
                    app.setAssignedTo(null);
                }
                applicationRepo.saveAll(assigned);
            }
        }

        // If deleting a claims officer, check for assigned claims
        if ("CLAIMS_OFFICER".equals(role)) {
            List<Claim> assignedClaims = claimRepo.findByReviewedBy(user);
            if (!assignedClaims.isEmpty()) {
                for (Claim claim : assignedClaims) {
                    claim.setReviewedBy(null);
                }
                claimRepo.saveAll(assignedClaims);
            }
        }

        userRepo.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users u = userRepo.findByEmail(email);
        if (u != null) {
            return User.builder()
                    .username(u.getEmail())
                    .password(u.getPassword())
                    .authorities("ROLE_" + u.getRole())
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
