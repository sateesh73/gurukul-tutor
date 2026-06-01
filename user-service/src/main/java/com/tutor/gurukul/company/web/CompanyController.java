package com.tutor.gurukul.company.web;

import com.tutor.gurukul.company.CompanyService;
import com.tutor.gurukul.company.exception.CompanyNotFoundException;
import com.tutor.gurukul.company.model.CompanyRequest;
import com.tutor.gurukul.company.model.CompanyResponse;
import com.tutor.gurukul.users.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing companies.
 * Provides endpoints to create, read, update, and delete company information.
 * This controller delegates business logic to the {@link CompanyService} and is responsible for handling HTTP requests and responses.
 * The controller methods are designed to follow RESTful conventions, using appropriate HTTP methods and status codes for each operation.
 * Recommended enhancements include:
 * - Adding validation annotations (e.g. @Valid) to request bodies to ensure that incoming data meets required constraints before being processed by the service layer.
 * - Implementing exception handling (e.g. @ControllerAdvice) to translate service exceptions into meaningful API error responses.
 * - Adding logging to track incoming requests and any errors that occur during processing.
 * - Implementing pagination for the getAllCompanies endpoint to handle large datasets efficiently.
 */
@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
class CompanyController {

    /** Service used to perform business logic related to company management.
     *
     * <p>This field is injected via Lombok's {@code @RequiredArgsConstructor} (constructor injection).
     * The concrete {@code CompanyService} implementation is expected to provide methods for creating, retrieving, updating, and deleting companies.
     * The controller delegates all business logic to this service and should not contain any direct data access or complex processing logic.
     */
    private final CompanyService companyService;

    /**
     * Retrieves a company by its ID.
     *
     * @param companyId the ID of the company to retrieve; must not be null.
     * @return a ResponseEntity containing the CompanyResponse with HTTP status 200 OK if found, or an appropriate error status if not found or if the request is invalid.
     */
    @GetMapping("/{companyId}")
    ResponseEntity<CompanyResponse> getCompanyById(@PathVariable String companyId) throws CompanyNotFoundException {
        return ResponseEntity.ok(companyService.getCompanyById(companyId));
    }

    /**
     * Retrieves all companies.
     *
     * @return a ResponseEntity containing a list of CompanyResponse objects with HTTP status 200 OK; the list will be empty if no companies are found.
     */
    @GetMapping
    ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    /**
     * Creates a new company based on the provided CompanyRequest.
     *
     * @param companyRequest the request body containing company details; must not be null and should contain required fields (e.g. name, email).
     * @return a ResponseEntity with HTTP status 201 Created and a Location header pointing to the newly created resource; returns an appropriate error status if the request is invalid.
     */
    @PostMapping
    ResponseEntity<Void> createCompany(@RequestBody CompanyRequest companyRequest) {
        companyService.createCompany(companyRequest);
        return ResponseEntity.created(URI.create("/api/v1/company")).build();
    }

    /**
     * Updates an existing company with the provided details.
     *
     * @param companyId the ID of the company to update; must not be null.
     * @param companyRequest the request body containing updated company details; must not be null and should contain required fields (e.g. name, email).
     * @return a ResponseEntity with HTTP status 204 No Content if the update is successful; returns an appropriate error status if the request is invalid or if the company is not found.
     */
    @PutMapping("/{companyId}")
    ResponseEntity<Void> updateCompany(@PathVariable String companyId, @RequestBody CompanyRequest companyRequest) throws CompanyNotFoundException {
        companyService.updateCompany(companyId, companyRequest);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a company by its ID.
     *
     * @param companyId the ID of the company to delete; must not be null.
     * @return a ResponseEntity with HTTP status 204 No Content if the deletion is successful; returns an appropriate error status if the request is invalid or if the company is not found.
     */
    @DeleteMapping("/{companyId}")
    ResponseEntity<Void> deleteCompany(@PathVariable String companyId) throws CompanyNotFoundException, UserNotFoundException {
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
