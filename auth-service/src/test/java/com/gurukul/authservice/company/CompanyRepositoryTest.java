package com.gurukul.authservice.company;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Company Repository Tests")
class CompanyRepositoryTest {

    @Autowired
    CompanyRepository companyRepository;

    @Test
    @DisplayName("Should save and find company by id")
    void saveAndFindById() {
        // Given - use unique code to avoid conflicts
        Company company = Company.builder()
                .name("Test Corp Unique1")
                .code("TEST001U1")
                .build();

        // When
        Company saved = companyRepository.save(company);

        // Then
        assertThat(saved.getCompanyId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Corp Unique1");

        Company found = companyRepository.findById(saved.getCompanyId()).orElseThrow();
        assertThat(found.getCode()).isEqualTo("TEST001U1");
    }

    @Test
    @DisplayName("Should find company by code")
    void findByCode() {
        // Given - use unique code
        companyRepository.save(Company.builder()
                .name("Found It Unique2")
                .code("FOUND002U2")
                .build());

        // When
        Company found = companyRepository.findByCode("FOUND002U2").orElseThrow();

        // Then
        assertThat(found.getName()).isEqualTo("Found It Unique2");
    }

    @Test
    @DisplayName("Should check if code exists")
    void existsByCode() {
        // Given - use unique code
        companyRepository.save(Company.builder()
                .name("Code Test Unique3")
                .code("CODE003U3")
                .build());

        // When & Then
        assertThat(companyRepository.existsByCode("CODE003U3")).isTrue();
        assertThat(companyRepository.existsByCode("OTHER004U4")).isFalse();
    }

    @Test
    @DisplayName("Should find all companies")
    void findAll() {
        // Given - create some test companies with unique codes
        // Note: There's also the Gurukul company created by AuthDataInitializer
        companyRepository.saveAll(List.of(
                Company.builder().name("Company A Unique5").code("COMP005U5").build(),
                Company.builder().name("Company B Unique6").code("COMP006U6").build()
        ));

        // When
        List<Company> companies = companyRepository.findAll();

        // Then - should have at least 3 (Gurukul + our 2 test companies)
        assertThat(companies).hasSizeGreaterThanOrEqualTo(3);
    }
}