package com.gurukul.authservice.company;

import com.gurukul.authservice.dto.CompanyDto;
import com.gurukul.authservice.dto.CreateCompanyRequest;
import com.gurukul.authservice.exception.DuplicateResourceException;
import com.gurukul.authservice.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public CompanyDto create(CreateCompanyRequest request) {
        if (companyRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Company code already exists: " + request.code());
        }
        Company company = Company.builder()
                .name(request.name())
                .code(request.code())
                .build();
        Company saved = companyRepository.save(company);
        return toDto(saved);
    }

    @Override
    public List<CompanyDto> findAll() {
        return companyRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CompanyDto findById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        return toDto(company);
    }

    @Override
    public CompanyDto update(Long companyId, CreateCompanyRequest request) {
        Company existing = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        existing.setName(request.name());
        existing.setCode(request.code());
        return toDto(companyRepository.save(existing));
    }

    @Override
    public void delete(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }
        companyRepository.deleteById(companyId);
    }

    @Override
    public boolean existsById(Long companyId) {
        return companyRepository.existsById(companyId);
    }

    @Override
    public boolean existsByCode(String code) {
        return companyRepository.existsByCode(code);
    }

    @Override
    public Long createCompany(String name, String code) {
        Company company = Company.builder()
                .name(name)
                .code(code)
                .build();
        Company saved = companyRepository.save(company);
        return saved.getCompanyId();
    }

    private CompanyDto toDto(Company company) {
        return new CompanyDto(
                company.getCompanyId(),
                company.getName(),
                company.getCode(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}