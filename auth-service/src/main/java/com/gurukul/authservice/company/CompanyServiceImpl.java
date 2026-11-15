package com.gurukul.authservice.company;

import com.gurukul.authservice.dto.CompanyDto;
import com.gurukul.authservice.dto.CreateCompanyRequest;
import com.gurukul.authservice.exception.DuplicateResourceException;
import com.gurukul.authservice.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        // Publish domain event to automatically provision the default company admin user
        eventPublisher.publishEvent(new CompanyCreatedEvent(
                saved.getCompanyId(),
                saved.getName(),
                saved.getCode(),
                request.adminEmail(),
                request.adminPassword()
        ));

        return toDto(saved);
    }

    @Override
    public List<CompanyDto> findAll() {
        return companyRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<CompanyDto> findAll(Pageable pageable) {
        return companyRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    public CompanyDto findById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        return toDto(company);
    }

    @Override
    public Optional<CompanyDto> findByCode(String code) {
        return companyRepository.findByCode(code).map(this::toDto);
    }

    @Override
    public CompanyDto update(Long companyId, CreateCompanyRequest request) {
        Company existing = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        if (!existing.getCode().equals(request.code()) && companyRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Company code already exists: " + request.code());
        }

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
        CompanyDto dto = create(new CreateCompanyRequest(name, code));
        return dto.companyId();
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