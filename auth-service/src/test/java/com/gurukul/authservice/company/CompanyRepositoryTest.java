package com.gurukul.authservice.company;

import com.gurukul.authservice.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Company Repository Tests with Testcontainers")
class CompanyRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    @DisplayName("Should save and find company by id with actual corporate data")
    void saveAndFindById() {
        // Given - Real partner enterprise data
        Company tcs = Company.builder()
                .name("Tata Consultancy Services")
                .code("TCS_GLOBAL")
                .build();

        // When
        Company saved = companyRepository.save(tcs);

        // Then
        assertThat(saved.getCompanyId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Tata Consultancy Services");
        assertThat(saved.getCode()).isEqualTo("TCS_GLOBAL");

        Company found = companyRepository.findById(saved.getCompanyId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Tata Consultancy Services");
        assertThat(found.getCode()).isEqualTo("TCS_GLOBAL");
    }

    @Test
    @DisplayName("Should find company by unique organization code")
    void findByCode() {
        // Given - Real learning institution
        companyRepository.save(Company.builder()
                .name("Infosys Learning Institute")
                .code("INFY_LEARN")
                .build());

        // When
        Company found = companyRepository.findByCode("INFY_LEARN").orElseThrow();

        // Then
        assertThat(found.getName()).isEqualTo("Infosys Learning Institute");
        assertThat(found.getCode()).isEqualTo("INFY_LEARN");
    }

    @Test
    @DisplayName("Should verify company code existence")
    void existsByCode() {
        // Given - Real corporate academy
        companyRepository.save(Company.builder()
                .name("Wipro Digital Academy")
                .code("WIPRO_ACADEMY")
                .build());

        // When & Then
        assertThat(companyRepository.existsByCode("WIPRO_ACADEMY")).isTrue();
        assertThat(companyRepository.existsByCode("NON_EXISTENT_INSTITUTE")).isFalse();
    }

    @Test
    @DisplayName("Should retrieve all registered partner organizations")
    void findAll() {
        // Given - Real higher education partner organizations
        companyRepository.saveAll(List.of(
                Company.builder().name("Stanford Extended Studies").code("STANFORD_EXT").build(),
                Company.builder().name("Harvard Professional Education").code("HARVARD_PRO").build()
        ));

        // When
        List<Company> companies = companyRepository.findAll();

        // Then - Should contain seeded Gurukul plus saved educational organizations
        assertThat(companies).hasSizeGreaterThanOrEqualTo(3);
        assertThat(companies)
                .extracting(Company::getName)
                .contains("Stanford Extended Studies", "Harvard Professional Education");
    }
}