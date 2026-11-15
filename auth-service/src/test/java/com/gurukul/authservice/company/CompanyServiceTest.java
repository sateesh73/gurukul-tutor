package com.gurukul.authservice.company;

import com.gurukul.authservice.BaseIntegrationTest;
import com.gurukul.authservice.dto.CompanyDto;
import com.gurukul.authservice.dto.CreateCompanyRequest;
import com.gurukul.authservice.exception.DuplicateResourceException;
import com.gurukul.authservice.exception.ResourceNotFoundException;
import com.gurukul.authservice.dto.UserDto;
import com.gurukul.authservice.user.Access;
import com.gurukul.authservice.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Company Service Tests with Testcontainers")
class CompanyServiceTest extends BaseIntegrationTest {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Should create a new partner institution with actual organizational data and auto-create company admin user")
    void createCompany() {
        // Given - Real edtech enterprise partner
        CreateCompanyRequest request = new CreateCompanyRequest("Acme Learning Technologies", "ACME_TECH");

        // When
        CompanyDto result = companyService.create(request);

        // Then
        assertThat(result.companyId()).isNotNull();
        assertThat(result.name()).isEqualTo("Acme Learning Technologies");
        assertThat(result.code()).isEqualTo("ACME_TECH");

        // Verify that default company admin user was automatically provisioned with same name and access ADMIN
        UserDto adminUser = userService.findByUsername("Acme Learning Technologies");
        assertThat(adminUser).isNotNull();
        assertThat(adminUser.companyId()).isEqualTo(result.companyId());
        assertThat(adminUser.access()).isEqualTo(Access.ADMIN);
    }

    @Test
    @DisplayName("Should throw exception when creating company with existing duplicate code")
    void createCompanyDuplicateCode() {
        // Given - Existing institution registered with code
        companyRepository.save(Company.builder()
                .name("Oracle Education Foundation")
                .code("ORCL_EDU")
                .build());

        CreateCompanyRequest duplicateRequest = new CreateCompanyRequest(
                "Oracle University Online", "ORCL_EDU");

        // When / Then
        assertThatThrownBy(() -> companyService.create(duplicateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Company code already exists: ORCL_EDU");
    }

    @Test
    @DisplayName("Should find all registered educational institutions")
    void findAllCompanies() {
        // Given - Save actual higher education institutions
        companyRepository.saveAll(List.of(
                Company.builder().name("Harvard Extension School").code("HARVARD_EXT").build(),
                Company.builder().name("Oxford Distance Learning").code("OXFORD_DIST").build()
        ));

        // When
        List<CompanyDto> result = companyService.findAll();

        // Then - Should contain Gurukul plus newly added partner institutions
        assertThat(result).hasSizeGreaterThanOrEqualTo(3);
        assertThat(result)
                .extracting(CompanyDto::name)
                .contains("Harvard Extension School", "Oxford Distance Learning");
    }

    @Test
    @DisplayName("Should find institution by primary key ID")
    void findCompanyById() {
        // Given - Real technical university partner
        Company saved = companyRepository.save(
                Company.builder().name("MIT Open Learning").code("MIT_OPEN").build());

        // When
        CompanyDto result = companyService.findById(saved.getCompanyId());

        // Then
        assertThat(result.name()).isEqualTo("MIT Open Learning");
        assertThat(result.code()).isEqualTo("MIT_OPEN");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when querying non-existent company ID")
    void findCompanyByIdNotFound() {
        assertThatThrownBy(() -> companyService.findById(999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found with id: 999999");
    }

    @Test
    @DisplayName("Should update partner institution name and code")
    void updateCompany() {
        // Given - Initial institution setup
        Company initial = companyRepository.save(
                Company.builder().name("IIT Bombay Academy").code("IITB_ACADEMY").build());

        CreateCompanyRequest updateRequest = new CreateCompanyRequest(
                "IIT Bombay Virtual Labs", "IITB_VLABS");

        // When
        CompanyDto result = companyService.update(initial.getCompanyId(), updateRequest);

        // Then
        assertThat(result.name()).isEqualTo("IIT Bombay Virtual Labs");
        assertThat(result.code()).isEqualTo("IITB_VLABS");
    }

    @Test
    @DisplayName("Should delete institution record successfully")
    void deleteCompany() {
        // Given - Temporary training center
        Company tempCenter = companyRepository.save(
                Company.builder().name("Temporary Training Center").code("TEMP_CENTER").build());

        // When
        companyService.delete(tempCenter.getCompanyId());

        // Then
        assertThatThrownBy(() -> companyService.findById(tempCenter.getCompanyId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}