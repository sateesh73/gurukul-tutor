package com.gurukul.authservice.web;

import com.gurukul.authservice.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("Auth Controller Tests with Testcontainers")
class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/auth/me - Should reject unauthenticated requests with 401 Unauthorized")
    void shouldReturnUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication required. Please provide a valid JWT token."));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Should return current administrator profile with valid JWT claims")
    void shouldReturnCurrentUserWithJwt() throws Exception {
        // "sateesh" is seeded in database by AuthDataInitializer on startup
        mockMvc.perform(get("/api/v1/auth/me")
                        .with(jwt().jwt(j -> j
                                .claim("preferred_username", "sateesh")
                                .claim("email", "sateesh@gurukul.com")
                                .claim("given_name", "Sateesh")
                                .claim("family_name", "Kumar"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("sateesh"))
                .andExpect(jsonPath("$.email").value("sateesh@gurukul.com"))
                .andExpect(jsonPath("$.access").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Should return member profile by X-User-Id header")
    void shouldReturnUserByHeader() throws Exception {
        // User with ID 1 is seeded administrator "sateesh"
        mockMvc.perform(get("/api/v1/auth/me")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh")))
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("sateesh"))
                .andExpect(jsonPath("$.access").value("ADMIN"));
    }
}
