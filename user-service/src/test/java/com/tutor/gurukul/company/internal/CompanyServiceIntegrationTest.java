package com.tutor.gurukul.company.internal;

import com.tutor.gurukul.TestcontainersConfiguration;
import com.tutor.gurukul.company.model.CompanyRequest;
import com.tutor.gurukul.company.model.CompanyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class CompanyServiceIntegrationTest {

    @Autowired
    CompanyService companyService;

    @Autowired
    CompanyRepo companyRepo;

    @BeforeEach
    void setUp() {
        companyRepo.deleteAll();
    }

    @Test
    void createCompany_persistsEntity() {
        var req = CompanyRequest.builder()
                .name("Acme")
                .email("info@acme.com")
                .description("Acme desc")
                .websiteUrl("https://acme.example")
                .logoUrl("/logos/acme.png")
                .build();

        companyService.createCompany(req);

        List<Company> all = companyRepo.findAll();
        assertEquals(1, all.size());
        Company saved = all.get(0);
        assertEquals("Acme", saved.getCompanyName());
        assertEquals("info@acme.com", saved.getEmail());
    }

    @Test
    void createCompany_twice_allowsDuplicates() {
        var req = CompanyRequest.builder().name("Acme").email("i@a").build();
        companyService.createCompany(req);
        companyService.createCompany(req);

        List<Company> all = companyRepo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void getCompanyById_returnsResponse() {
        Company toSave = Company.builder().companyName("X").email("x@x").build();
        Company saved = companyRepo.save(toSave);

        CompanyResponse resp = companyService.getCompanyById(saved.getCompanyId());

        assertEquals(saved.getCompanyId(), resp.id());
        assertEquals("X", resp.name());
    }

    @Test
    void updateCompany_updatesFields() {
        Company saved = companyRepo.save(Company.builder().companyName("Old").email("old@old").build());

        var req = CompanyRequest.builder().name("New").email("new@e").description("d").websiteUrl("w").logoUrl("l").build();
        companyService.updateCompany(saved.getCompanyId(), req);

        Company updated = companyRepo.findById(saved.getCompanyId()).orElseThrow();
        assertEquals("New", updated.getCompanyName());
        assertEquals("new@e", updated.getEmail());
    }

    @Test
    void deleteCompany_removesEntity() {
        Company saved = companyRepo.save(Company.builder().companyName("Del").email("d@d").build());
        companyService.deleteCompany(saved.getCompanyId());
        assertFalse(companyRepo.findById(saved.getCompanyId()).isPresent());
    }

    @Test
    void deleteCompany_nonexistent_throws() {
        assertThrows(IllegalArgumentException.class, () -> companyService.deleteCompany("no-such-id"));
    }
}
