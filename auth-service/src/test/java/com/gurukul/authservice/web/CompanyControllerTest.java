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
@DisplayName("Company Controller Tests")
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/companies - Should create company via REST API")
    void shouldCreateCompany() throws Exception {
        String requestBody = """
                {
                    "name": "Test Company API Unique1",
                    "code": "TESTAPI001UNIQ1"
                }
                """;

        mockMvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/companies/")))
                .andExpect(jsonPath("$.name").value("Test Company API Unique1"))
                .andExpect(jsonPath("$.code").value("TESTAPI001UNIQ1"));
    }

    @Test
    @DisplayName("GET /api/v1/companies - Should get all companies via REST API")
    void shouldGetAllCompanies() throws Exception {
        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/companies/{id} - Should get company by id via REST API")
    void shouldGetCompanyById() throws Exception {
        // Create a company
        String createBody = """
                {
                    "name": "Get Company Test Unique2",
                    "code": "GETCOMP002UNIQ2"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/companies/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long companyId = extractCompanyId(responseBody);

        // Then get the company
        mockMvc.perform(get("/api/v1/companies/" + companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Get Company Test Unique2"));
    }

    @Test
    @DisplayName("PUT /api/v1/companies/{id} - Should update company via REST API")
    void shouldUpdateCompany() throws Exception {
        // Create a company first
        String createBody = """
                {
                    "name": "Update Company Test Unique3",
                    "code": "UPDATE002UNIQ3"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/companies/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long companyId = extractCompanyId(responseBody);

        // Then update the company
        String updateBody = """
                {
                    "name": "Updated Company Test Unique4",
                    "code": "UPDATED002UNIQ4"
                }
                """;

        mockMvc.perform(put("/api/v1/companies/" + companyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Company Test Unique4"))
                .andExpect(jsonPath("$.code").value("UPDATED002UNIQ4"));
    }

    @Test
    @DisplayName("DELETE /api/v1/companies/{id} - Should delete company via REST API")
    void shouldDeleteCompany() throws Exception {
        // Create a company to delete
        String createBody = """
                {
                    "name": "Delete Company Test Unique5",
                    "code": "DELETE002UNIQ5"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/companies/")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long companyId = extractCompanyId(responseBody);

        // Delete company
        mockMvc.perform(delete("/api/v1/companies/" + companyId))
                .andExpect(status().isNoContent());
    }

    private long extractCompanyId(String json) {
        // Extract companyId from JSON response like {"companyId":123,...}
        String companyIdStr = json.replaceAll(".*\"companyId\":(\\d+).*", "$1");
        return Long.parseLong(companyIdStr);
    }
}