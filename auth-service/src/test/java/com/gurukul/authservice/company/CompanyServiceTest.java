package com.gurukul.authservice.company;

import com.gurukul.authservice.dto.CompanyDto;
import com.gurukul.authservice.dto.CreateCompanyRequest;
import com.gurukul.authservice.exception.DuplicateResourceException;
import com.gurukul.authservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Company Service Tests")
class CompanyServiceTest {

    @Autowired
    CompanyService companyService;

    @Autowired
    CompanyRepository companyRepository;

    @Test
    @DisplayName("Should create a new company")
    void createCompany() {
        // Given - use unique code
        CreateCompanyRequest request = new CreateCompanyRequest("Acme Corp Unique1", "ACME001U1");

        // When
        CompanyDto result = companyService.create(request);

        // Then
        assertThat(result.companyId()).isNotNull();
        assertThat(result.name()).isEqualTo("Acme Corp Unique1");
        assertThat(result.code()).isEqualTo("ACME001U1");
    }

    @Test
    @DisplayName("Should throw exception when creating company with duplicate code")
    void createCompanyDuplicateCode() {
        // Given - existing company with unique code
        companyRepository.save(Company.builder().name("Existing U2").code("DUP002U2").build());
        CreateCompanyRequest request = new CreateCompanyRequest("New Company U3", "DUP002U2");

        // When / Then
        assertThatThrownBy(() -> companyService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Company code already exists");
    }

    @Test
    @DisplayName("Should find all companies")
    void findAllCompanies() {
        // Given - create test companies with unique codes
        // Note: There's also the Gurukul company created by AuthDataInitializer
        // Use highly unique codes to avoid conflicts with UserServiceTest.findByCompanyId
        companyRepository.saveAll(List.of(
                Company.builder().name("Company A U4").code("COMP004U4SEPDV").build(),
                Company.builder().name("Company B U5").code("COMP005U5SEPDV").build()
        ));

        // When
        List<CompanyDto> result = companyService.findAll();

        // Then - should have at least 3 (Gurukul + our 2 test companies)
        assertThat(result).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should find company by id")
    void findCompanyById() {
        // Given - use unique code
        Company saved = companyRepository.save(
                Company.builder().name("FindMe U6").code("FIND006U6").build());

        // When
        CompanyDto result = companyService.findById(saved.getCompanyId());

        // Then
        assertThat(result.name()).isEqualTo("FindMe U6");
        assertThat(result.code()).isEqualTo("FIND006U6");
    }

    @Test
    @DisplayName("Should throw exception when company not found by id")
    void findCompanyByIdNotFound() {
        assertThatThrownBy(() -> companyService.findById(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");
    }

    @Test
    @DisplayName("Should update company")
    void updateCompany() {
        // Given - use unique code
        Company saved = companyRepository.save(
                Company.builder().name("Old Name U7").code("OLD007U7").build());
        CreateCompanyRequest request = new CreateCompanyRequest("New Name U8", "NEW008U8");

        // When
        CompanyDto result = companyService.update(saved.getCompanyId(), request);

        // Then
        assertThat(result.name()).isEqualTo("New Name U8");
        assertThat(result.code()).isEqualTo("NEW008U8");
    }

    @Test
    @DisplayName("Should delete company")
    void deleteCompany() {
        // Given - use unique code
        Company saved = companyRepository.save(
                Company.builder().name("Delete Me U9").code("DEL009U9").build());

        // When
        companyService.delete(saved.getCompanyId());

        // Then
        assertThatThrownBy(() -> companyService.findById(saved.getCompanyId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}