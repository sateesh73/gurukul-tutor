package com.gurukul.authservice.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Controller Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/users - Should create user via REST API")
    void shouldCreateUser() throws Exception {
        // Gurukul company (ID 1) is created by AuthDataInitializer on startup
        String requestBody = """
                {
                    "username": "testuser_api_unique1",
                    "email": "testuser_api_unique1@example.com",
                    "password": "password123",
                    "companyId": 1,
                    "role": "USER"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/users/")))
                .andExpect(jsonPath("$.username").value("testuser_api_unique1"))
                .andExpect(jsonPath("$.email").value("testuser_api_unique1@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users - Should get all users via REST API")
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Should get user by id via REST API")
    void shouldGetUserById() throws Exception {
        // Gurukul company (ID 1) is created by AuthDataInitializer on startup
        // Use unique username/email to avoid conflicts
        String createBody = """
                {
                    "username": "getuser_test_unique2",
                    "email": "getuser_test_unique2@example.com",
                    "password": "password123",
                    "companyId": 1,
                    "role": "USER"
                }
                """;

        // Create user and extract the ID from Location header
        String responseBody = mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/users/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract userId from response
        long userId = extractUserId(responseBody);

        // Then get the user
        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("getuser_test_unique2"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - Should update user via REST API")
    void shouldUpdateUser() throws Exception {
        // Gurukul company (ID 1) is created by AuthDataInitializer on startup
        String createBody = """
                {
                    "username": "updateuser_test_unique3",
                    "email": "updateuser_test_unique3@example.com",
                    "password": "password123",
                    "companyId": 1,
                    "role": "USER"
                }
                """;

        // Create user and extract the ID
        String responseBody = mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/users/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = extractUserId(responseBody);

        // Then update the user
        String updateBody = """
                {
                    "username": "updateduser_unique",
                    "email": "updateduser_unique@example.com",
                    "password": "newpassword",
                    "companyId": 1,
                    "role": "USER"
                }
                """;

        mockMvc.perform(put("/api/v1/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser_unique"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - Should delete user via REST API")
    void shouldDeleteUser() throws Exception {
        // Gurukul company (ID 1) is created by AuthDataInitializer on startup
        String createBody = """
                {
                    "username": "deleteuser_test_unique4",
                    "email": "deleteuser_test_unique4@example.com",
                    "password": "password123",
                    "companyId": 1,
                    "role": "USER"
                }
                """;

        // Create user and extract the ID
        String responseBody = mockMvc.perform(post("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/users/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = extractUserId(responseBody);

        // Delete user
        mockMvc.perform(delete("/api/v1/users/" + userId))
                .andExpect(status().isNoContent());
    }

    private long extractUserId(String json) {
        // Extract userId from JSON response like {"userId":123,...}
        String userIdStr = json.replaceAll(".*\"userId\":(\\d+).*", "$1");
        return Long.parseLong(userIdStr);
    }
}