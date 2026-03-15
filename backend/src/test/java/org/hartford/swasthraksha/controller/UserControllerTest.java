package org.hartford.swasthraksha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.swasthraksha.config.SecurityConfig;
import org.hartford.swasthraksha.exception.GlobalExceptionHandler;
import org.hartford.swasthraksha.filter.JwtFilter;
import org.hartford.swasthraksha.model.Users;
import org.hartford.swasthraksha.repository.UserRepository;
import org.hartford.swasthraksha.service.AuthService;
import org.hartford.swasthraksha.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController.
 *
 * Layer    : Controller (Web Layer only — no DB, no full Spring context)
 * Tool     : @WebMvcTest + MockMvc
 * Mocking  : @MockBean replaces AuthService and UserRepository
 * Auth     : POST /register is public; admin endpoints require admin role
 * Naming   : methodName_whenCondition_shouldExpectedResult
 *
 * Note: AuthService implements UserDetailsService, so @MockBean AuthService
 * satisfies the UserDetailsService injection required by JwtFilter.
 */
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtFilter.class, JwtUtil.class})
class UserControllerTest {

    // ── Constants ──────────────────────────────────────────────────────────
    private static final String REGISTER_URL        = "/register";
    private static final String ADMIN_USERS_URL     = "/admin/users";
    private static final String ADMIN_OFFICERS_URL  = "/admin/claims-officers";
    private static final String DELETE_USER_URL     = "/admin/users/{id}";

    private static final String ADMIN_EMAIL         = "admin@test.com";
    private static final String APPLICANT_EMAIL     = "applicant@test.com";

    // ── Beans ──────────────────────────────────────────────────────────────
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private UserRepository userRepository;

    // ── Fixtures ───────────────────────────────────────────────────────────
    private Users applicant;
    private Users underwriter;
    private Users claimsOfficer;

    @BeforeEach
    void setUp() {
        applicant = new Users();
        applicant.setId(1L);
        applicant.setUsername("John Doe");
        applicant.setEmail("new-applicant@test.com");
        applicant.setRole("APPLICANT");

        underwriter = new Users();
        underwriter.setId(2L);
        underwriter.setUsername("Jane Smith");
        underwriter.setEmail("new-underwriter@test.com");
        underwriter.setRole("UNDERWRITER");

        claimsOfficer = new Users();
        claimsOfficer.setId(3L);
        claimsOfficer.setUsername("Bob Officer");
        claimsOfficer.setEmail("new-officer@test.com");
        claimsOfficer.setRole("CLAIMS_OFFICER");
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /register
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("register - when valid new user submits registration - should return 201 with user body")
    void register_whenValidNewUser_shouldReturn201WithUserBody() throws Exception {
        when(authService.register(any(Users.class))).thenReturn(applicant);

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicant)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("new-applicant@test.com")))
                .andExpect(jsonPath("$.role", is("APPLICANT")));

        verify(authService).register(any(Users.class));
    }

    @Test
    @DisplayName("register - when email already exists - should return 400 with error message")
    void register_whenDuplicateEmail_shouldReturn400WithErrorMessage() throws Exception {
        when(authService.register(any(Users.class)))
                .thenThrow(new RuntimeException("This email is already registered: new-applicant@test.com"));

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicant)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    @DisplayName("register - when request body is missing - should return 4xx error")
    void register_whenRequestBodyMissing_shouldReturn4xxError() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verifyNoInteractions(authService);
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /admin/users  (register underwriter)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registerUnderwriter - when admin registers new underwriter - should return 201 with UNDERWRITER role")
    void registerUnderwriter_whenAdminRegisters_shouldReturn201WithUnderwriterRole() throws Exception {
        when(authService.registerUnderwriter(any(Users.class))).thenReturn(underwriter);

        mockMvc.perform(post(ADMIN_USERS_URL)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(underwriter)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("new-underwriter@test.com")))
                .andExpect(jsonPath("$.role", is("UNDERWRITER")));

        verify(authService).registerUnderwriter(any(Users.class));
    }

    @Test
    @DisplayName("registerUnderwriter - when non-admin calls endpoint - should return 403 Forbidden")
    void registerUnderwriter_whenNonAdminCalls_shouldReturn403() throws Exception {
        mockMvc.perform(post(ADMIN_USERS_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(underwriter)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("registerUnderwriter - when unauthenticated - should return 401 Unauthorized")
    void registerUnderwriter_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post(ADMIN_USERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(underwriter)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("registerUnderwriter - when email already exists - should return 400 with error message")
    void registerUnderwriter_whenDuplicateEmail_shouldReturn400() throws Exception {
        when(authService.registerUnderwriter(any(Users.class)))
                .thenThrow(new RuntimeException("This email is already registered: new-underwriter@test.com"));

        mockMvc.perform(post(ADMIN_USERS_URL)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(underwriter)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /admin/users  (list underwriters)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getUnderwriters - when admin requests underwriter list - should return 200 with list")
    void getUnderwriters_whenAdminRequests_shouldReturn200WithList() throws Exception {
        when(userRepository.findByRoleContaining("UNDERWRITER")).thenReturn(List.of(underwriter));

        mockMvc.perform(get(ADMIN_USERS_URL)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is("new-underwriter@test.com")))
                .andExpect(jsonPath("$[0].role", is("UNDERWRITER")));

        verify(userRepository).findByRoleContaining("UNDERWRITER");
    }

    @Test
    @DisplayName("getUnderwriters - when no underwriters exist - should return 200 with empty list")
    void getUnderwriters_whenNoUnderwriters_shouldReturn200WithEmptyList() throws Exception {
        when(userRepository.findByRoleContaining("UNDERWRITER")).thenReturn(List.of());

        mockMvc.perform(get(ADMIN_USERS_URL)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("getUnderwriters - when non-admin requests - should return 403 Forbidden")
    void getUnderwriters_whenNonAdminRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(ADMIN_USERS_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getUnderwriters - when unauthenticated - should return 401 Unauthorized")
    void getUnderwriters_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(ADMIN_USERS_URL))
                .andExpect(status().isUnauthorized());
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /admin/claims-officers  (register claims officer)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("registerClaimsOfficer - when admin registers new officer - should return 201 with CLAIMS_OFFICER role")
    void registerClaimsOfficer_whenAdminRegisters_shouldReturn201WithClaimsOfficerRole() throws Exception {
        when(authService.registerClaimsOfficer(any(Users.class))).thenReturn(claimsOfficer);

        mockMvc.perform(post(ADMIN_OFFICERS_URL)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimsOfficer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("new-officer@test.com")))
                .andExpect(jsonPath("$.role", is("CLAIMS_OFFICER")));

        verify(authService).registerClaimsOfficer(any(Users.class));
    }

    @Test
    @DisplayName("registerClaimsOfficer - when non-admin calls endpoint - should return 403 Forbidden")
    void registerClaimsOfficer_whenNonAdminCalls_shouldReturn403() throws Exception {
        mockMvc.perform(post(ADMIN_OFFICERS_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimsOfficer)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("registerClaimsOfficer - when unauthenticated - should return 401 Unauthorized")
    void registerClaimsOfficer_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post(ADMIN_OFFICERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimsOfficer)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("registerClaimsOfficer - when email already exists - should return 400 with error message")
    void registerClaimsOfficer_whenDuplicateEmail_shouldReturn400() throws Exception {
        when(authService.registerClaimsOfficer(any(Users.class)))
                .thenThrow(new RuntimeException("This email is already registered: new-officer@test.com"));

        mockMvc.perform(post(ADMIN_OFFICERS_URL)
                        .with(user(ADMIN_EMAIL).roles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimsOfficer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    // ══════════════════════════════════════════════════════════════════════
    // GET /admin/claims-officers  (list claims officers)
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getClaimsOfficers - when admin requests officer list - should return 200 with list")
    void getClaimsOfficers_whenAdminRequests_shouldReturn200WithList() throws Exception {
        when(userRepository.findByRoleContaining("CLAIMS_OFFICER")).thenReturn(List.of(claimsOfficer));

        mockMvc.perform(get(ADMIN_OFFICERS_URL)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role", is("CLAIMS_OFFICER")));

        verify(userRepository).findByRoleContaining("CLAIMS_OFFICER");
    }

    @Test
    @DisplayName("getClaimsOfficers - when non-admin requests - should return 403 Forbidden")
    void getClaimsOfficers_whenNonAdminRequests_shouldReturn403() throws Exception {
        mockMvc.perform(get(ADMIN_OFFICERS_URL)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("getClaimsOfficers - when unauthenticated - should return 401 Unauthorized")
    void getClaimsOfficers_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get(ADMIN_OFFICERS_URL))
                .andExpect(status().isUnauthorized());
    }

    // ══════════════════════════════════════════════════════════════════════
    // DELETE /admin/users/{id}
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteUser - when admin deletes existing user - should return 200 with success message")
    void deleteUser_whenAdminDeletesExistingUser_shouldReturn200WithMessage() throws Exception {
        doNothing().when(authService).deleteUser(1L);

        mockMvc.perform(delete(DELETE_USER_URL, 1L)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("deleted")));

        verify(authService).deleteUser(1L);
    }

    @Test
    @DisplayName("deleteUser - when non-admin calls delete - should return 403 Forbidden")
    void deleteUser_whenNonAdminCalls_shouldReturn403() throws Exception {
        mockMvc.perform(delete(DELETE_USER_URL, 1L)
                        .with(user(APPLICANT_EMAIL).roles("APPLICANT")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("deleteUser - when unauthenticated - should return 401 Unauthorized")
    void deleteUser_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(delete(DELETE_USER_URL, 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("deleteUser - when user ID not found - should return 400 with error message")
    void deleteUser_whenUserNotFound_shouldReturn400() throws Exception {
        doThrow(new IllegalArgumentException("User not found with id: 999"))
                .when(authService).deleteUser(999L);

        mockMvc.perform(delete(DELETE_USER_URL, 999L)
                        .with(user(ADMIN_EMAIL).roles("admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("999")));
    }
}
