package com.gurukul.authservice.web;

import com.gurukul.authservice.BaseIntegrationTest;
import com.gurukul.authservice.company.CompanyService;
import com.gurukul.authservice.dto.CompanyDto;
import com.gurukul.authservice.dto.CreateCompanyRequest;
import com.gurukul.authservice.dto.CreateUserRequest;
import com.gurukul.authservice.dto.UserDto;
import com.gurukul.authservice.user.Access;
import com.gurukul.authservice.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("Multi-Tenant Isolation and Multi-Company Authorization Integration Tests")
class MultiTenantAuthorizationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Only Gurukul Admin can create a company; auto-provisions company admin user with same name and ADMIN access")
    void gurukulAdminCreatesCompanyAndProvisionsAdminUser() throws Exception {
        String companyJson = """
                {
                    "name": "Apex Edutech Global",
                    "code": "APEX_EDU",
                    "adminEmail": "admin@apexedutech.com",
                    "adminPassword": "ApexPassword#2026"
                }
                """;

        // Gurukul Admin (companyId = 1, role = ADMIN, username = sateesh)
        String response = mockMvc.perform(post("/api/v1/companies")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "sateesh")
                                .claim("company_id", "1")
                                .claim("roles", java.util.List.of("ROLE_ADMIN", "ROLE_GURUKUL_ADMIN"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(companyJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apex Edutech Global"))
                .andExpect(jsonPath("$.code").value("APEX_EDU"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long createdCompanyId = Long.parseLong(response.replaceAll(".*\"companyId\":(\\d+).*", "$1"));

        // Verify that default company admin user with SAME NAME as company and role ADMIN was automatically provisioned
        UserDto autoProvisionedUser = userService.findByUsername("Apex Edutech Global");
        assertThat(autoProvisionedUser).isNotNull();
        assertThat(autoProvisionedUser.companyId()).isEqualTo(createdCompanyId);
        assertThat(autoProvisionedUser.access()).isEqualTo(Access.ADMIN);
        assertThat(autoProvisionedUser.email()).isEqualTo("admin@apexedutech.com");
    }

    @Test
    @DisplayName("Non-Gurukul admin CANNOT create a company - Rejected with 403 Forbidden")
    void nonGurukulUserCannotCreateCompany() throws Exception {
        // Register an actual tenant company first
        CompanyDto tenant = companyService.create(new CreateCompanyRequest("Tenant B", "TENANT_B"));

        String createCompanyPayload = """
                {
                    "name": "Illegal Sub Tenant",
                    "code": "ILLEGAL_SUB"
                }
                """;

        // Tenant B admin attempts to create a company
        mockMvc.perform(post("/api/v1/companies")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "tenant_b_admin")
                                .claim("company_id", String.valueOf(tenant.companyId()))
                                .claim("roles", java.util.List.of("ROLE_ADMIN"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createCompanyPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value(containsString("Only Gurukul Admin")));
    }

    @Test
    @DisplayName("Company user can only view their own company; cannot view other company details")
    void tenantCannotAccessOtherCompanyData() throws Exception {
        CompanyDto companyX = companyService.create(new CreateCompanyRequest("Company Alpha", "COMP_ALPHA"));
        CompanyDto companyY = companyService.create(new CreateCompanyRequest("Company Beta", "COMP_BETA"));

        // Company Alpha user can view Company Alpha
        mockMvc.perform(get("/api/v1/companies/" + companyX.companyId())
                        .with(jwt().jwt(j -> j.claim("preferred_username", "alpha_user")
                                .claim("company_id", String.valueOf(companyX.companyId()))
                                .claim("roles", java.util.List.of("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMP_ALPHA"));

        // Company Alpha user CANNOT view Company Beta (returns 403 Forbidden)
        mockMvc.perform(get("/api/v1/companies/" + companyY.companyId())
                        .with(jwt().jwt(j -> j.claim("preferred_username", "alpha_user")
                                .claim("company_id", String.valueOf(companyX.companyId()))
                                .claim("roles", java.util.List.of("ROLE_ADMIN")))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        // Company Alpha user calling GET /api/v1/companies only gets their own company
        mockMvc.perform(get("/api/v1/companies")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "alpha_user")
                                .claim("company_id", String.valueOf(companyX.companyId()))
                                .claim("roles", java.util.List.of("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].companyId").value(companyX.companyId()));
    }

    @Test
    @DisplayName("Company admin CANNOT create users under another company - Rejected with 403 Forbidden")
    void tenantCannotCreateUserUnderAnotherCompany() throws Exception {
        CompanyDto company1 = companyService.create(new CreateCompanyRequest("Tech Corp 1", "TC_1"));
        CompanyDto company2 = companyService.create(new CreateCompanyRequest("Tech Corp 2", "TC_2"));

        String illegalUserPayload = String.format("""
                {
                    "username": "infiltrator.user",
                    "email": "infiltrator@techcorp2.com",
                    "password": "SecurePassword#2026",
                    "companyId": %d,
                    "role": "TEACHER"
                }
                """, company2.companyId());

        // Tech Corp 1 admin tries to create user under Tech Corp 2
        mockMvc.perform(post("/api/v1/users")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "tc1_admin")
                                .claim("company_id", String.valueOf(company1.companyId()))
                                .claim("roles", java.util.List.of("ROLE_ADMIN"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(illegalUserPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value(containsString("cannot create users for company")));
    }

    @Test
    @DisplayName("Company admin can successfully create users under their OWN company")
    void tenantCanCreateUserUnderOwnCompany() throws Exception {
        CompanyDto myCompany = companyService.create(new CreateCompanyRequest("Delta Learning", "DELTA_LRN"));

        String validUserPayload = String.format("""
                {
                    "username": "delta.faculty.member",
                    "email": "faculty@deltalearning.com",
                    "password": "DeltaPassword#2026",
                    "companyId": %d,
                    "role": "TEACHER"
                }
                """, myCompany.companyId());

        // Delta Learning admin creates user under Delta Learning
        mockMvc.perform(post("/api/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                                .jwt(j -> j.claim("preferred_username", "delta_admin")
                                        .claim("company_id", String.valueOf(myCompany.companyId()))
                                        .claim("roles", java.util.List.of("ROLE_ADMIN"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUserPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("delta.faculty.member"))
                .andExpect(jsonPath("$.companyId").value(myCompany.companyId()))
                .andExpect(jsonPath("$.access").value("TEACHER"));
    }

    @Test
    @DisplayName("A company user CANNOT see users belonging to other companies")
    void tenantCannotSeeUsersFromOtherCompany() throws Exception {
        CompanyDto compA = companyService.create(new CreateCompanyRequest("Omega Group A", "OMEGA_A"));
        CompanyDto compB = companyService.create(new CreateCompanyRequest("Omega Group B", "OMEGA_B"));

        // User under Comp B
        UserDto compBUser = userService.create(new CreateUserRequest(
                "student.comp.b",
                "student.b@omegab.com",
                "StudentPass#2026",
                compB.companyId(),
                "STUDENT"
        ));

        // Comp A user calls GET /api/v1/users - Should ONLY see Comp A users, never Comp B
        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "comp_a_admin")
                                .claim("company_id", String.valueOf(compA.companyId()))
                                .claim("roles", java.util.List.of("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].companyId", everyItem(is(compA.companyId().intValue()))))
                .andExpect(jsonPath("$[*].username", not(hasItem("student.comp.b"))));

        // Comp A user attempts to directly access Comp B user by ID - Returns 403 Forbidden
        mockMvc.perform(get("/api/v1/users/" + compBUser.userId())
                        .with(jwt().jwt(j -> j.claim("preferred_username", "comp_a_admin")
                                .claim("company_id", String.valueOf(compA.companyId()))
                                .claim("roles", java.util.List.of("ROLE_ADMIN")))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));

        // Comp A user attempts to query Comp B users via /company/{companyId} - Returns 403 Forbidden
        mockMvc.perform(get("/api/v1/users/company/" + compB.companyId())
                        .with(jwt().jwt(j -> j.claim("preferred_username", "comp_a_admin")
                                .claim("company_id", String.valueOf(compA.companyId()))
                                .claim("roles", java.util.List.of("ROLE_ADMIN")))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }
}
