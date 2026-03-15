package org.hartford.swasthraksha.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hartford.swasthraksha.config.SecurityConfig;
import org.hartford.swasthraksha.dto.JwtRequest;
import org.hartford.swasthraksha.exception.GlobalExceptionHandler;
import org.hartford.swasthraksha.filter.JwtFilter;
import org.hartford.swasthraksha.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController.
 *
 * Layer    : Controller (Web Layer only — no DB, no full Spring context)
 * Tool     : @WebMvcTest + MockMvc
 * Mocking  : @MockBean replaces AuthenticationManager
 * Auth     : /login is a public endpoint — no JWT required
 * Naming   : methodName_whenCondition_shouldExpectedResult
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtFilter.class, JwtUtil.class})
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthenticationManager authManager;
    @MockBean private UserDetailsService userDetailsService;

    private static final String LOGIN_URL = "/login";

    /** Builds a JwtRequest DTO. */
    private JwtRequest buildLoginRequest(String email, String password) {
        JwtRequest req = new JwtRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    // ══════════════════════════════════════════════════════════════════════
    // POST /login
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("login - when valid credentials provided - should return 200 with JWT token in body")
    void login_whenValidCredentials_shouldReturn200WithToken() throws Exception {
        UserDetails userDetails = User.withUsername("user@test.com")
                .password("encoded-password")
                .authorities("ROLE_APPLICANT")
                .build();
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authManager.authenticate(any())).thenReturn(authToken);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLoginRequest("user@test.com", "pass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.token", not(emptyString())));

        verify(authManager).authenticate(any());
    }

    @Test
    @DisplayName("login - when credentials are invalid - should return 4xx with error message")
    void login_whenInvalidCredentials_shouldReturn4xxWithErrorMessage() throws Exception {
        when(authManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password."));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLoginRequest("wrong@test.com", "bad"))))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", notNullValue()));

        verify(authManager).authenticate(any());
    }

    @Test
    @DisplayName("login - when authentication manager throws RuntimeException - should return 400 Bad Request")
    void login_whenRuntimeExceptionThrown_shouldReturn400() throws Exception {
        when(authManager.authenticate(any()))
                .thenThrow(new RuntimeException("Unexpected authentication error"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLoginRequest("user@test.com", "pass"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Unexpected authentication error")));
    }

    @Test
    @DisplayName("login - when request body is missing - should return 4xx error")
    void login_whenRequestBodyMissing_shouldReturn4xxError() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        verifyNoInteractions(authManager);
    }

    @Test
    @DisplayName("login - when request content type is missing - should return 4xx error")
    void login_whenContentTypeMissing_shouldReturn4xxError() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .content("{\"email\":\"user@test.com\",\"password\":\"pass\"}"))
                .andExpect(status().is4xxClientError());
    }
}
