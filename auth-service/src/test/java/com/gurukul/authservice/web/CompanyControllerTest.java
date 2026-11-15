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
@DisplayName("Company Controller Tests with Testcontainers")
class CompanyControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/companies - Should reject unauthenticated requests with 401 Unauthorized")
    void shouldReturnUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication required. Please provide a valid JWT token."));
    }

    @Test
    @DisplayName("POST /api/v1/companies - Should register educational partner via REST API with valid JWT")
    void shouldCreateCompany() throws Exception {
        String requestBody = """
                {
                    "name": "Tata Institute of Fundamental Research",
                    "code": "TIFR_MUMBAI"
                }
                """;

        mockMvc.perform(post("/api/v1/companies")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/companies/")))
                .andExpect(jsonPath("$.name").value("Tata Institute of Fundamental Research"))
                .andExpect(jsonPath("$.code").value("TIFR_MUMBAI"));
    }

    @Test
    @DisplayName("GET /api/v1/companies - Should retrieve all registered partner organizations with JWT")
    void shouldGetAllCompanies() throws Exception {
        mockMvc.perform(get("/api/v1/companies")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/companies/{id} - Should fetch company profile by ID with JWT")
    void shouldGetCompanyById() throws Exception {
        // Register an actual institution first
        String createBody = """
                {
                    "name": "Indian Institute of Science",
                    "code": "IISC_BLR"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/companies")
                                .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/companies/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long companyId = extractCompanyId(responseBody);

        // Fetch the institution
        mockMvc.perform(get("/api/v1/companies/" + companyId)
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Indian Institute of Science"))
                .andExpect(jsonPath("$.code").value("IISC_BLR"));
    }

    @Test
    @DisplayName("PUT /api/v1/companies/{id} - Should update institution details with JWT")
    void shouldUpdateCompany() throws Exception {
        // Register initial company
        String createBody = """
                {
                    "name": "National University of Singapore",
                    "code": "NUS_INIT"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/companies")
                                .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long companyId = extractCompanyId(responseBody);

        // Update the institution
        String updateBody = """
                {
                    "name": "National University of Singapore - Global Campus",
                    "code": "NUS_GLOBAL"
                }
                """;

        mockMvc.perform(put("/api/v1/companies/" + companyId)
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("National University of Singapore - Global Campus"))
                .andExpect(jsonPath("$.code").value("NUS_GLOBAL"));
    }

    @Test
    @DisplayName("DELETE /api/v1/companies/{id} - Should delete partner institution with JWT")
    void shouldDeleteCompany() throws Exception {
        // Register temporary institution
        String createBody = """
                {
                    "name": "Decommissioned Testing Center",
                    "code": "DECOM_TEST_CTR"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/companies")
                                .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long companyId = extractCompanyId(responseBody);

        // Delete institution
        mockMvc.perform(delete("/api/v1/companies/" + companyId)
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh.kumar"))))
                .andExpect(status().isNoContent());
    }

    private long extractCompanyId(String json) {
        String companyIdStr = json.replaceAll(".*\"companyId\":(\\d+).*", "$1");
        return Long.parseLong(companyIdStr);
    }
}