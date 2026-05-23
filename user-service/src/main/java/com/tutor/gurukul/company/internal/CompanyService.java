package com.tutor.gurukul.company.internal;

import com.tutor.gurukul.company.model.CompanyRequest;
import com.tutor.gurukul.company.model.CompanyResponse;

public interface CompanyService {
    void createCompany(CompanyRequest companyRequest);
    CompanyResponse getCompanyById(String companyId);
    void updateCompany(String companyId, CompanyRequest companyRequest);
    void deleteCompany(String companyId);
}
