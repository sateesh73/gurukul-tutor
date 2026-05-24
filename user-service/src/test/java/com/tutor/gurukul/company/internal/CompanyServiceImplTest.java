package com.tutor.gurukul.company.internal;

import com.tutor.gurukul.company.exception.CompanyAlreadyExistsException;
import com.tutor.gurukul.company.exception.CompanyNotFoundException;
import com.tutor.gurukul.company.model.CompanyRequest;
import com.tutor.gurukul.company.model.CompanyResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    CompanyRepo companyRepo;

    @InjectMocks
    CompanyServiceImpl companyService;

    @Test
    void createCompany_success_savesEntity() {
        var req = CompanyRequest.builder()
                .name("Acme")
                .email("info@acme.com")
                .description("Acme desc")
                .websiteUrl("https://acme.example")
                .logoUrl("/logos/acme.png")
                .build();

        when(companyRepo.findById("Acme")).thenReturn(Optional.empty());
        when(companyRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        companyService.createCompany(req);

        verify(companyRepo).save(argThat(c -> c.getCompanyName().equals("Acme") &&
                c.getEmail().equals("info@acme.com") &&
                c.getDescription().equals("Acme desc")
        ));
    }

    @Test
    void createCompany_alreadyExists_throwsCompanyAlreadyExistsException() {
        var req = CompanyRequest.builder().name("Acme").build();
        var existing = Company.builder().companyId("1").companyName("Acme").email("e@e").build();
        when(companyRepo.findById("Acme")).thenReturn(Optional.of(existing));

        var ex = assertThrows(RuntimeException.class, () -> companyService.createCompany(req));
        assertTrue(ex.getMessage().contains("Failed to create company") || ex.getMessage().contains("already exists"));
    }

    @Test
    void getCompanyById_found_returnsResponse() throws CompanyNotFoundException {
        var company = Company.builder()
                .companyId("id-1")
                .companyName("Acme")
                .email("a@a")
                .description("desc")
                .websiteUrl("https://acme")
                .logoUrl("/logo.png")
                .build();

        when(companyRepo.findById("id-1")).thenReturn(Optional.of(company));

        CompanyResponse resp = companyService.getCompanyById("id-1");

        assertEquals("id-1", resp.id());
        assertEquals("Acme", resp.name());
        assertEquals("a@a", resp.email());
    }

    @Test
    void getCompanyById_notFound_throwsCompanyNotFoundException() {
        when(companyRepo.findById("nope")).thenReturn(Optional.empty());
        assertThrows(CompanyNotFoundException.class, () -> companyService.getCompanyById("nope"));
    }

    @Test
    void updateCompany_success_savesUpdatedEntity() throws CompanyNotFoundException {
        var existing = Company.builder()
                .companyId("id-1")
                .companyName("Old")
                .email("old@old")
                .build();
        when(companyRepo.findById("id-1")).thenReturn(Optional.of(existing));
        when(companyRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = CompanyRequest.builder().name("NewName").email("new@e").description("newdesc").websiteUrl("w").logoUrl("l").build();

        companyService.updateCompany("id-1", req);

        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepo).save(captor.capture());
        Company saved = captor.getValue();
        assertEquals("id-1", saved.getCompanyId());
        assertEquals("NewName", saved.getCompanyName());
        assertEquals("new@e", saved.getEmail());
    }

    @Test
    void updateCompany_notFound_throwsCompanyNotFoundException() {
        when(companyRepo.findById("missing")).thenReturn(Optional.empty());
        var req = CompanyRequest.builder().name("n").build();
        assertThrows(CompanyNotFoundException.class, () -> companyService.updateCompany("missing", req));
    }

    @Test
    void deleteCompany_success_deletesById() throws CompanyNotFoundException {
        var existing = Company.builder().companyId("id-1").companyName("Acme").email("e@e").build();
        when(companyRepo.findById("id-1")).thenReturn(Optional.of(existing));

        companyService.deleteCompany("id-1");

        verify(companyRepo).deleteById("id-1");
    }

    @Test
    void deleteCompany_notFound_throwsCompanyNotFoundException() {
        when(companyRepo.findById("x")).thenReturn(Optional.empty());
        assertThrows(CompanyNotFoundException.class, () -> companyService.deleteCompany("x"));
    }
}
