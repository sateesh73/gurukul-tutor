package com.tutor.gurukul.company.internal;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing Company entities. This interface extends JpaRepository, providing CRUD operations and query methods for Company objects.
 * The primary key type for Company is String, which represents the company ID.
 * By extending JpaRepository, this interface inherits methods for saving, deleting, and finding Company entities, as well as pagination and sorting capabilities.
 * This repository will be used by the CompanyService to perform database operations related to Company entities, such as creating, retrieving, updating, and deleting company records.
 * Implementations of this interface will be automatically provided by Spring Data JPA at runtime, allowing for easy integration with the underlying database without the need for boilerplate code.
 */
interface CompanyRepo extends JpaRepository<Company, String> {
}
