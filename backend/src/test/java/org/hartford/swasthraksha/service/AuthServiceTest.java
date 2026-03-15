package org.hartford.swasthraksha.service;

import org.hartford.swasthraksha.model.Application;
import org.hartford.swasthraksha.model.Claim;
import org.hartford.swasthraksha.model.Users;
import org.hartford.swasthraksha.repository.ApplicationRepository;
import org.hartford.swasthraksha.repository.ClaimRepository;
import org.hartford.swasthraksha.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 *
 * Layer   : Service (no Spring context)
 * Tool    : @ExtendWith(MockitoExtension.class) + @Mock + @InjectMocks
 * Naming  : methodName_whenCondition_shouldExpectedResult
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepo;
    @Mock private ApplicationRepository applicationRepo;
    @Mock private ClaimRepository claimRepo;
    @Mock private PasswordEncoder pe;

    @InjectMocks private AuthService authService;

    private Users applicant;
    private Users underwriter;
    private Users claimsOfficer;

    @BeforeEach
    void setUp() {
        applicant = new Users();
        applicant.setId(1L);
        applicant.setUsername("John Doe");
        applicant.setEmail("applicant@test.com");
        applicant.setPassword("raw-password");
        applicant.setRole("APPLICANT");

        underwriter = new Users();
        underwriter.setId(2L);
        underwriter.setUsername("Jane Smith");
        underwriter.setEmail("underwriter@test.com");
        underwriter.setPassword("raw-password");
        underwriter.setRole("UNDERWRITER");

        claimsOfficer = new Users();
        claimsOfficer.setId(3L);
        claimsOfficer.setUsername("Bob Officer");
        claimsOfficer.setEmail("claims@test.com");
        claimsOfficer.setPassword("raw-password");
        claimsOfficer.setRole("CLAIMS_OFFICER");
    }

    // ══════════════════════════════════════════════════════════════════════
    // register
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("register - when email is new - should encode password and assign APPLICANT role")
    void register_whenNewEmail_shouldEncodePasswordAndAssignApplicantRole() {
        Users input = new Users();
        input.setEmail("new@test.com");
        input.setPassword("plain");

        when(userRepo.findByEmail("new@test.com")).thenReturn(null);
        when(pe.encode("plain")).thenReturn("encoded");
        when(userRepo.save(any(Users.class))).thenAnswer(inv -> inv.getArgument(0));

        Users result = authService.register(input);

        assertThat(result.getRole()).isEqualTo("APPLICANT");
        assertThat(result.getPassword()).isEqualTo("encoded");
        verify(userRepo).save(input);
    }

    @Test
    @DisplayName("register - when email already registered - should throw RuntimeException without saving")
    void register_whenDuplicateEmail_shouldThrowRuntimeExceptionWithoutSaving() {
        when(userRepo.findByEmail(applicant.getEmail())).thenReturn(applicant);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(applicant));

        assertThat(ex.getMessage()).contains("already registered");
        verify(userRepo, never()).save(any());
    }

    @Test
    @DisplayName("register - when called with null password - should still call encoder and save")
    void register_whenNullPassword_shouldCallEncoderAndSave() {
        Users input = new Users();
        input.setEmail("nullpass@test.com");
        input.setPassword(null);

        when(userRepo.findByEmail("nullpass@test.com")).thenReturn(null);
        when(pe.encode(null)).thenReturn("encoded-null");
        when(userRepo.save(any(Users.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> authService.register(input));
        verify(pe).encode(null);
    }

    // ══════════════════════════════════════════════════════════════════════
    // registerUnderwriter
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registerUnderwriter - when email is new - should set role UNDERWRITER and encode password")
    void registerUnderwriter_whenNewEmail_shouldSetUnderwriterRole() {
        Users input = new Users();
        input.setEmail("new-uw@test.com");
        input.setPassword("plain");

        when(userRepo.findByEmail("new-uw@test.com")).thenReturn(null);
        when(pe.encode("plain")).thenReturn("encoded");
        when(userRepo.save(any(Users.class))).thenAnswer(inv -> inv.getArgument(0));

        Users result = authService.registerUnderwriter(input);

        assertThat(result.getRole()).isEqualTo("UNDERWRITER");
        assertThat(result.getPassword()).isEqualTo("encoded");
        verify(userRepo).save(input);
    }

    @Test
    @DisplayName("registerUnderwriter - when email already registered - should throw RuntimeException")
    void registerUnderwriter_whenDuplicateEmail_shouldThrowRuntimeException() {
        when(userRepo.findByEmail(underwriter.getEmail())).thenReturn(underwriter);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.registerUnderwriter(underwriter));

        assertThat(ex.getMessage()).contains("already registered");
        verify(userRepo, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // registerClaimsOfficer
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registerClaimsOfficer - when email is new - should set role CLAIMS_OFFICER")
    void registerClaimsOfficer_whenNewEmail_shouldSetClaimsOfficerRole() {
        Users input = new Users();
        input.setEmail("new-co@test.com");
        input.setPassword("plain");

        when(userRepo.findByEmail("new-co@test.com")).thenReturn(null);
        when(pe.encode("plain")).thenReturn("encoded");
        when(userRepo.save(any(Users.class))).thenAnswer(inv -> inv.getArgument(0));

        Users result = authService.registerClaimsOfficer(input);

        assertThat(result.getRole()).isEqualTo("CLAIMS_OFFICER");
        verify(userRepo).save(input);
    }

    @Test
    @DisplayName("registerClaimsOfficer - when email already registered - should throw RuntimeException")
    void registerClaimsOfficer_whenDuplicateEmail_shouldThrowRuntimeException() {
        when(userRepo.findByEmail(claimsOfficer.getEmail())).thenReturn(claimsOfficer);

        assertThrows(RuntimeException.class,
                () -> authService.registerClaimsOfficer(claimsOfficer));

        verify(userRepo, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    // deleteUser
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteUser - when user ID not found - should throw IllegalArgumentException")
    void deleteUser_whenUserNotFound_shouldThrowIllegalArgumentException() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.deleteUser(999L));

        assertThat(ex.getMessage()).contains("999");
        verify(userRepo, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteUser - when UNDERWRITER has assigned applications - should nullify assignments before deleting")
    void deleteUser_whenUnderwriterHasAssignedApps_shouldNullifyAssignmentsAndDelete() {
        when(userRepo.findById(2L)).thenReturn(Optional.of(underwriter));

        Application app = new Application();
        app.setAssignedTo(underwriter);

        when(applicationRepo.findByAssignedToEmail(underwriter.getEmail()))
                .thenReturn(List.of(app));
        when(applicationRepo.saveAll(anyList())).thenReturn(List.of(app));

        authService.deleteUser(2L);

        assertThat(app.getAssignedTo()).isNull();
        verify(applicationRepo).saveAll(anyList());
        verify(userRepo).deleteById(2L);
    }

    @Test
    @DisplayName("deleteUser - when UNDERWRITER has no assigned applications - should delete without saveAll")
    void deleteUser_whenUnderwriterHasNoAssignedApps_shouldDeleteWithoutSaveAll() {
        when(userRepo.findById(2L)).thenReturn(Optional.of(underwriter));
        when(applicationRepo.findByAssignedToEmail(underwriter.getEmail()))
                .thenReturn(List.of());

        authService.deleteUser(2L);

        verify(applicationRepo, never()).saveAll(anyList());
        verify(userRepo).deleteById(2L);
    }

    @Test
    @DisplayName("deleteUser - when CLAIMS_OFFICER has assigned claims - should nullify reviewedBy before deleting")
    void deleteUser_whenClaimsOfficerHasAssignedClaims_shouldNullifyReviewedByAndDelete() {
        when(userRepo.findById(3L)).thenReturn(Optional.of(claimsOfficer));

        Claim claim = new Claim();
        claim.setReviewedBy(claimsOfficer);

        when(claimRepo.findByReviewedBy(claimsOfficer)).thenReturn(List.of(claim));
        when(claimRepo.saveAll(anyList())).thenReturn(List.of(claim));

        authService.deleteUser(3L);

        assertThat(claim.getReviewedBy()).isNull();
        verify(claimRepo).saveAll(anyList());
        verify(userRepo).deleteById(3L);
    }

    @Test
    @DisplayName("deleteUser - when CLAIMS_OFFICER has no assigned claims - should delete without saveAll")
    void deleteUser_whenClaimsOfficerHasNoClaims_shouldDeleteWithoutSaveAll() {
        when(userRepo.findById(3L)).thenReturn(Optional.of(claimsOfficer));
        when(claimRepo.findByReviewedBy(claimsOfficer)).thenReturn(List.of());

        authService.deleteUser(3L);

        verify(claimRepo, never()).saveAll(anyList());
        verify(userRepo).deleteById(3L);
    }

    @Test
    @DisplayName("deleteUser - when APPLICANT - should delete directly without touching applications or claims")
    void deleteUser_whenApplicant_shouldDeleteDirectlyWithoutNullifyingOtherEntities() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(applicant));

        authService.deleteUser(1L);

        verify(applicationRepo, never()).findByAssignedToEmail(any());
        verify(claimRepo, never()).findByReviewedBy(any());
        verify(userRepo).deleteById(1L);
    }

    // ══════════════════════════════════════════════════════════════════════
    // loadUserByUsername
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("loadUserByUsername - when email exists - should return UserDetails with ROLE_ prefix on authority")
    void loadUserByUsername_whenValidEmail_shouldReturnUserDetailsWithRolePrefix() {
        when(userRepo.findByEmail(applicant.getEmail())).thenReturn(applicant);

        UserDetails details = authService.loadUserByUsername(applicant.getEmail());

        assertThat(details.getUsername()).isEqualTo(applicant.getEmail());
        assertThat(details.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_APPLICANT");
    }

    @Test
    @DisplayName("loadUserByUsername - when email does not exist - should throw UsernameNotFoundException")
    void loadUserByUsername_whenEmailNotFound_shouldThrowUsernameNotFoundException() {
        when(userRepo.findByEmail("ghost@test.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> authService.loadUserByUsername("ghost@test.com"));
    }

    @Test
    @DisplayName("loadUserByUsername - when user is UNDERWRITER - should reflect ROLE_UNDERWRITER authority")
    void loadUserByUsername_whenUnderwriter_shouldReturnUnderwriterAuthority() {
        when(userRepo.findByEmail(underwriter.getEmail())).thenReturn(underwriter);

        UserDetails details = authService.loadUserByUsername(underwriter.getEmail());

        assertThat(details.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_UNDERWRITER");
    }
}
