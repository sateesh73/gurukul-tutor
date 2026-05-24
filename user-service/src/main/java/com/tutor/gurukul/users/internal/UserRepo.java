package com.tutor.gurukul.users.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing User entities. This interface extends JpaRepository, providing CRUD operations and query methods for User objects.
 * The primary key type for User is String, which represents the user ID.
 * By extending JpaRepository, this interface inherits methods for saving, deleting, and finding User entities, as well as pagination and sorting capabilities.
 * This repository will be used by the UserService to perform database operations related to User entities, such as creating, retrieving, updating, and deleting user records.
 * Implementations of this interface will be automatically provided by Spring Data JPA at runtime, allowing for easy integration with the underlying database without the need for boilerplate code.
 */
interface UserRepo extends JpaRepository<Users, String> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address of the user to find; must not be null and should be a valid email format.
     * @return an Optional containing the User entity if found, or an empty Optional if no user is found with the given email.
     */
    Optional<Users> findByEmail(String email);
    /**
     * Finds users by their associated company ID.
     *
     * @param companyId the ID of the company to find users for; must not be null.
     * @return an Optional containing a list of User entities associated with the given company ID, or an empty Optional if no users are found for that company.
     */
    Optional<List<Users>> findByCompanyId(String companyId);
}
