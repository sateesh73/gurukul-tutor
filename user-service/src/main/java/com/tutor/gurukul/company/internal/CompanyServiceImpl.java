package com.tutor.gurukul.company.internal;

import com.tutor.gurukul.company.model.CompanyRequest;
import com.tutor.gurukul.company.model.CompanyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link CompanyService} that provides business logic for managing companies.
 */
@RequiredArgsConstructor
@Slf4j
@Service
class CompanyServiceImpl implements CompanyService {

    /**
     * Repository used to persist and query {@code Company} entities.
     *
     * <p>This field is injected via Lombok's {@code @RequiredArgsConstructor} (constructor injection).
     * The concrete {@code CompanyRepo} is expected to provide basic CRUD operations such as
     * {@code findById}, {@code save}, and {@code deleteById}. Implementations should handle
     * repository exceptions (e.g. Spring's {@code DataAccessException}) at the service boundary
     * as appropriate for the application.
     */
    private final CompanyRepo companyRepo;

    /**
     * Creates a new company from the given {@link CompanyRequest}.
     *
     * <p>Expected responsibilities:
     * - Validate {@code companyRequest} (null-checks and business validation).
     * - Map the request to a {@code Company} entity.
     * - Persist the entity using {@code companyRepo}.
     * - Return or publish created resource information if needed (this method is {@code void}
     *   so the API design may return created identifiers elsewhere).
     *
     * <p>Recommended enhancements (not implemented here):
     * - Annotate with a transaction boundary (e.g. {@code @Transactional}) to ensure atomicity.
     * - Use Bean Validation (e.g. {@code @Valid}) on the request object at the controller boundary.
     * - Handle and translate repository exceptions into domain/service exceptions or API errors.
     *
     * @param companyRequest the request containing company details; must not be {@code null}
     *                       and should contain required fields (e.g. name or email) per business rules.
     * @throws IllegalArgumentException if {@code companyRequest} is invalid (recommended).
     */
    @Override
    public void createCompany(CompanyRequest companyRequest) {
        log.info("Creating company: {}", companyRequest.name());
        try {
            if (companyRepo.findById(companyRequest.name()).isPresent()) {
                throw new IllegalArgumentException("Company with name " + companyRequest.name() + " already exists");
            }
            var company = companyRequestTo(companyRequest);
            companyRepo.save(company);
        } catch (Exception e) {
            log.error("Error while creating company: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to create company", e);
        }

    }

    /**
     * Retrieves a company by its identifier and converts it to a DTO.
     *
     * <p>Behavior:
     * - Uses {@code companyRepo.findById(companyId)} to fetch the entity.
     * - Converts the entity to {@link CompanyResponse} using {@link #companyResponseTo(Company)}.
     * - If no entity is found, this implementation calls {@code Optional.orElseThrow()},
     *   which will throw {@link java.util.NoSuchElementException}.
     *
     * <p>Notes:
     * - Consider throwing a more specific domain exception (e.g. {@code CompanyNotFoundException})
     *   so callers (controllers) can map the exception to an appropriate HTTP status (404).
     *
     * @param companyId the identifier of the company to retrieve; must not be {@code null}.
     * @return the {@link CompanyResponse} corresponding to the persisted company.
     * @throws java.util.NoSuchElementException if the company with {@code companyId} does not exist.
     */
    @Override
    public CompanyResponse getCompanyById(String companyId) {
        log.info("Retrieving company by ID: {}", companyId);
        return companyRepo.findById(companyId)
                .map(this::companyResponseTo)
                .orElseThrow();
    }

    /**
     * Updates an existing company identified by {@code companyId} using values from
     * {@link CompanyRequest}.
     *
     * <p>Expected responsibilities:
     * - Validate {@code companyRequest} and {@code companyId}.
     * - Load the existing entity (e.g. {@code companyRepo.findById(companyId)}).
     * - Apply changes from the request to the entity (only allowed/updatable fields).
     * - Persist the updated entity (e.g. {@code companyRepo.save(entity)}).
     *
     * <p>Recommended enhancements (not implemented here):
     * - Annotate with {@code @Transactional}.
     * - If the entity is not found, throw a domain-specific exception such as
     *   {@code CompanyNotFoundException}.
     * - Implement partial updates (patch) or full replace semantics consistently.
     *
     * @param companyId      the identifier of the company to update; must not be {@code null}.
     * @param companyRequest the request containing updated company details.
     * @throws java.util.NoSuchElementException if the company with {@code companyId} does not exist (recommended).
     * @throws IllegalArgumentException if {@code companyRequest} is invalid (recommended).
     */
    @Override
    public void updateCompany(String companyId, CompanyRequest companyRequest) {
        log.info("Updating company with ID: {}", companyId);
        try {
            var company = companyRepo.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company with ID " + companyId + " not found"));
            var updatedCompany = Company.builder()
                    .companyId(company.getCompanyId())
                    .companyName(companyRequest.name())
                    .email(companyRequest.email())
                    .description(companyRequest.description())
                    .websiteUrl(companyRequest.websiteUrl())
                    .logoUrl(companyRequest.logoUrl())
                    .build();
            companyRepo.save(updatedCompany);
        } catch (Exception e) {
            log.error("Validation error while updating company: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to update company: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes the company identified by {@code companyId}.
     *
     * <p>Behavior to implement:
     * - Option A: Call {@code companyRepo.deleteById(companyId)} directly (no-op if not present
     *   depending on repository implementation).
     * - Option B: Validate existence first via {@code findById} and throw a domain-specific
     *   exception if not found (preferred if callers expect a 404 when deleting missing resources).
     *
     * <p>Recommended enhancements:
     * - Wrap delete operation in a transaction if deletion cascades to related entities.
     * - Consider soft-delete semantics (a {@code deleted} flag) if the domain requires audit/history.
     *
     * @param companyId the identifier of the company to delete; must not be {@code null}.
     */
    @Override
    public void deleteCompany(String companyId) {
        companyRepo.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company with ID " + companyId + " not found"));
        companyRepo.deleteById(companyId);
    }

    /**
     * Helper which maps a persistence {@link Company} entity to a {@link CompanyResponse}.
     *
     * <p>Mapping rules:
     * - Maps identifier, name, description, website and logo fields from the entity to the DTO.
     * - Preserves null values as-is.
     * - Add additional fields or transform as needed (e.g. build absolute URLs for logos).
     *
     * <p>Notes:
     * - If mapping logic grows, consider extracting to a dedicated mapper class (MapStruct or
     *   a small utility) to keep service code focused on business logic.
     *
     * @param company the persistence entity to convert; must not be {@code null}.
     * @return a {@link CompanyResponse} with fields copied from {@code company}.
     */
    private CompanyResponse companyResponseTo(Company company) {
        return CompanyResponse.builder()
                .id(company.getCompanyId())
                .name(company.getCompanyName())
                .email(company.getEmail())
                .description(company.getDescription())
                .websiteUrl(company.getWebsiteUrl())
                .logoUrl(company.getLogoUrl())
                .build();
    }

    /**
     * Helper which maps a {@link CompanyRequest} to a persistence {@link Company} entity.
     *
     * <p>Mapping rules:
     * - Maps name, description, website and logo fields from the request to the entity.
     * - Preserves null values as-is.
     *
     * <p>Notes:
     * - If mapping logic grows, consider extracting to a dedicated mapper class (MapStruct or
     *   a small utility) to keep service code focused on business logic.
     *
     * @param companyRequest the request containing company details; must not be {@code null}.
     * @return a {@link Company} entity with fields copied from {@code companyRequest}.
     *
     */
    private Company companyRequestTo(CompanyRequest companyRequest) {
        return Company.builder()
                .companyName(companyRequest.name())
                .email(companyRequest.email())
                .description(companyRequest.description())
                .websiteUrl(companyRequest.websiteUrl())
                .logoUrl(companyRequest.logoUrl())
                .build();
    }
}