package com.gurukul.authservice.web;

import com.gurukul.authservice.company.CompanyService;
import com.gurukul.authservice.dto.CompanyDto;
import com.gurukul.authservice.dto.CreateCompanyRequest;
import com.gurukul.authservice.security.TenantSecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Validated
public class CompanyController {

    private final CompanyService companyService;
    private final TenantSecurityService tenantSecurity;

    @PostMapping
    public ResponseEntity<CompanyDto> create(@Valid @RequestBody CreateCompanyRequest request) {
        if (!tenantSecurity.isGurukulAdmin()) {
            throw new AccessDeniedException("Access denied: Only Gurukul Admin has permission to register a company.");
        }
        CompanyDto created = companyService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/companies/" + created.companyId()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<CompanyDto>> findAll() {
        if (tenantSecurity.isGurukulAdmin()) {
            return ResponseEntity.ok(companyService.findAll());
        }
        Long companyId = tenantSecurity.getCurrentCompanyId();
        if (companyId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(List.of(companyService.findById(companyId)));
    }

    @GetMapping("/page")
    public ResponseEntity<Page<CompanyDto>> findAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        if (tenantSecurity.isGurukulAdmin()) {
            return ResponseEntity.ok(companyService.findAll(pageable));
        }
        Long companyId = tenantSecurity.getCurrentCompanyId();
        if (companyId == null) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
        return ResponseEntity.ok(companyService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> findById(@PathVariable Long id) {
        if (!tenantSecurity.canAccessCompany(id)) {
            throw new AccessDeniedException("Access denied: Cannot access company details for company ID " + id);
        }
        return ResponseEntity.ok(companyService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateCompanyRequest request) {
        if (!tenantSecurity.canAccessCompany(id)) {
            throw new AccessDeniedException("Access denied: Cannot modify company details for company ID " + id);
        }
        if (!tenantSecurity.isGurukulAdmin()) {
            CompanyDto current = companyService.findById(id);
            if (!current.code().equals(request.code())) {
                throw new AccessDeniedException("Access denied: Only Gurukul Admin has permission to change company code.");
            }
        }
        return ResponseEntity.ok(companyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!tenantSecurity.isGurukulAdmin()) {
            throw new AccessDeniedException("Access denied: Only Gurukul Admin has permission to delete a company.");
        }
        companyService.delete(id);
    }
}