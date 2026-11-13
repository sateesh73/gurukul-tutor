package com.gurukul.authservice.company;

import com.gurukul.authservice.dto.CompanyDto;
import com.gurukul.authservice.dto.CreateCompanyRequest;

import java.util.List;

public interface CompanyService {
    CompanyDto create(CreateCompanyRequest request);
    List<CompanyDto> findAll();
    CompanyDto findById(Long companyId);
    CompanyDto update(Long companyId, CreateCompanyRequest request);
    void delete(Long companyId);
    boolean existsById(Long companyId);
    boolean existsByCode(String code);
    Long createCompany(String name, String code);
}