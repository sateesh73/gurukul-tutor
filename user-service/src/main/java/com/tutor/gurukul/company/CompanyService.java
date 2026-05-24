package com.tutor.gurukul.company;

import com.tutor.gurukul.company.exception.CompanyAlreadyExistsException;
import com.tutor.gurukul.company.exception.CompanyNotFoundException;
import com.tutor.gurukul.company.model.CompanyRequest;
import com.tutor.gurukul.company.model.CompanyResponse;

import java.util.List;

/**
 * Service interface for managing company-related operations.
 * This interface defines the contract for creating, retrieving, updating, and deleting company information.
 * Implementations of this interface will handle the business logic for managing companies, including interactions with the database through the CompanyRepo.
 * The methods in this interface are designed to be used by the CompanyController to provide RESTful API endpoints for company management.
 * The service layer is responsible for validating input, handling exceptions, and ensuring that business rules are enforced when performing operations on company data.
 * Recommended enhancements for implementations include:
 * - Adding transaction management (e.g. @Transactional) to ensure data integrity during create/update/delete operations.
 * - Implementing caching for read operations to improve performance.
 * - Using Bean Validation (e.g. @Valid) on request objects at the controller boundary to ensure that incoming data meets required constraints before reaching the service layer.
 */
public interface CompanyService {
    /**
     * Creates a new company based on the provided CompanyRequest.
     *
     * @param companyRequest the request object containing company details; must not be null and should contain required fields (e.g. name, email).
     * @throws CompanyAlreadyExistsException if the companyRequest is invalid or if a company with the same name already exists.
     */
    void createCompany(CompanyRequest companyRequest) throws CompanyAlreadyExistsException;
    /**
     * Retrieves a company by its ID.
     *
     * @param companyId the ID of the company to retrieve; must not be null.
     * @return the CompanyResponse object containing the company details; will be null if no company is found with the given ID.
     * @throws CompanyNotFoundException if the companyId is null.
     */
    CompanyResponse getCompanyById(String companyId) throws CompanyNotFoundException;
    /**
     * Updates an existing company with the provided details.
     *
     * @param companyId the ID of the company to update; must not be null.
     * @param companyRequest the request object containing updated company details; must not be null and should contain required fields (e.g. name, email).
     * @throws CompanyNotFoundException if the companyId or companyRequest is null, or if a company with the same name already exists.
     */
    void updateCompany(String companyId, CompanyRequest companyRequest) throws CompanyNotFoundException, RuntimeException;
    /**
     * Deletes a company by its ID.
     *
     * @param companyId the ID of the company to delete; must not be null.
     * @throws CompanyNotFoundException if the companyId is null.
     */
    void deleteCompany(String companyId) throws CompanyNotFoundException;
    /**
     * Retrieves all companies.
     *
     * @return a list of CompanyResponse objects containing all companies; will be empty if no companies are found.
     */
    List<CompanyResponse> getAllCompanies();
}
