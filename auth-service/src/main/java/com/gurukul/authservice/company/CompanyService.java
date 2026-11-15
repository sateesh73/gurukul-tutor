package com.gurukul.authservice.company;

import com.gurukul.authservice.dto.CompanyDto;
import com.gurukul.authservice.dto.CreateCompanyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CompanyService {
    CompanyDto create(CreateCompanyRequest request);
    List<CompanyDto> findAll();
    Page<CompanyDto> findAll(Pageable pageable);
    CompanyDto findById(Long companyId);
    Optional<CompanyDto> findByCode(String code);
    CompanyDto update(Long companyId, CreateCompanyRequest request);
    void delete(Long companyId);
    boolean existsById(Long companyId);
    boolean existsByCode(String code);
    Long createCompany(String name, String code);
}