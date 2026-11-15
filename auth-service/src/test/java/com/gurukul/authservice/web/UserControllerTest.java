package com.gurukul.authservice.web;

import com.gurukul.authservice.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("User Controller Tests with Testcontainers")
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/users - Should reject unauthenticated requests with 401 Unauthorized")
    void shouldReturnUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication required. Please provide a valid JWT token."));
    }

    @Test
    @DisplayName("POST /api/v1/users - Should register new academic faculty via REST API with valid JWT")
    void shouldCreateUser() throws Exception {
        // Enrolled under Gurukul (ID 1)
        String requestBody = """
                {
                    "username": "dr.vikas.swarup",
                    "email": "vikas.swarup@gurukul.com",
                    "password": "FacultySecure@2026",
                    "companyId": 1,
                    "role": "TEACHER"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/users/")))
                .andExpect(jsonPath("$.username").value("dr.vikas.swarup"))
                .andExpect(jsonPath("$.email").value("vikas.swarup@gurukul.com"))
                .andExpect(jsonPath("$.access").value("TEACHER"));
    }

    @Test
    @DisplayName("GET /api/v1/users - Should retrieve all registered members via REST API with JWT")
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Should fetch user profile by ID with JWT")
    void shouldGetUserById() throws Exception {
        // Create user
        String createBody = """
                {
                    "username": "kavita.krishnan",
                    "email": "kavita.krishnan@tcs.com",
                    "password": "TcsPassword#2026",
                    "companyId": 1,
                    "role": "ASSOCIATE"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/users")
                                .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/users/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = extractUserId(responseBody);

        // Fetch user
        mockMvc.perform(get("/api/v1/users/" + userId)
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("kavita.krishnan"))
                .andExpect(jsonPath("$.email").value("kavita.krishnan@tcs.com"))
                .andExpect(jsonPath("$.access").value("ASSOCIATE"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - Should update member details with JWT")
    void shouldUpdateUser() throws Exception {
        // Create user first
        String createBody = """
                {
                    "username": "alok.nath",
                    "email": "alok.nath@gurukul.com",
                    "password": "InitialPassword@2026",
                    "companyId": 1,
                    "role": "STUDENT"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/users")
                                .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = extractUserId(responseBody);

        // Update to faculty
        String updateBody = """
                {
                    "username": "prof.alok.nath",
                    "email": "prof.alok.nath@gurukul.com",
                    "password": "PromotedPassword#2026",
                    "companyId": 1,
                    "role": "TEACHER"
                }
                """;

        mockMvc.perform(put("/api/v1/users/" + userId)
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("prof.alok.nath"))
                .andExpect(jsonPath("$.email").value("prof.alok.nath@gurukul.com"))
                .andExpect(jsonPath("$.access").value("TEACHER"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - Should delete member account via REST API with JWT")
    void shouldDeleteUser() throws Exception {
        // Create user to delete
        String createBody = """
                {
                    "username": "sunita.williams",
                    "email": "sunita.williams@nasa.gov",
                    "password": "NasaPassword!2026",
                    "companyId": 1,
                    "role": "STUDENT"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/users")
                                .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = extractUserId(responseBody);

        // Delete user
        mockMvc.perform(delete("/api/v1/users/" + userId)
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar"))))
                .andExpect(status().isNoContent());
    }

    private long extractUserId(String json) {
        String userIdStr = json.replaceAll(".*\"userId\":(\\d+).*", "$1");
        return Long.parseLong(userIdStr);
    }
}