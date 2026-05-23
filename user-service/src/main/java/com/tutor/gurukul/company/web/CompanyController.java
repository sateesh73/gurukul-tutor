package com.tutor.gurukul.company.web;

import com.tutor.gurukul.company.internal.CompanyService;
import com.tutor.gurukul.company.model.CompanyRequest;
import com.tutor.gurukul.company.model.CompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{companyId}")
    ResponseEntity<CompanyResponse> getCompanyById(@PathVariable String companyId) {
        return ResponseEntity.ok(companyService.getCompanyById(companyId));
    }

    @PostMapping
    ResponseEntity<Void> createCompany(@RequestBody CompanyRequest companyRequest) {
        companyService.createCompany(companyRequest);
        return ResponseEntity.created(URI.create("/api/v1/company")).build();
    }

    @PutMapping("/{companyId}")
    ResponseEntity<Void> updateCompany(@PathVariable String companyId, @RequestBody CompanyRequest companyRequest) {
        companyService.updateCompany(companyId, companyRequest);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{companyId}")
    ResponseEntity<Void> deleteCompany(@PathVariable String companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
