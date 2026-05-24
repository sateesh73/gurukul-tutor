package com.tutor.gurukul.users;

import com.tutor.gurukul.users.exception.UserNotFoundException;
import com.tutor.gurukul.users.model.UserRequest;
import com.tutor.gurukul.users.model.UserResponse;

import java.util.List;

/**
 * Service interface for managing user-related operations.
 * This interface defines the contract for creating, retrieving, updating, and deleting user information.
 * Implementations of this interface will handle the business logic for managing users, including interactions with the database through the UserRepo.
 * The methods in this interface are designed to be used by the UserController to provide RESTful API endpoints for user management.
 * The service layer is responsible for validating input, handling exceptions, and ensuring that business rules are enforced when performing operations on user data.
 * Recommended enhancements for implementations include:
 * - Adding transaction management (e.g. @Transactional) to ensure data integrity during create/update/delete operations.
 * - Implementing caching for read operations to improve performance.
 * - Using Bean Validation (e.g. @Valid) on request objects at the controller boundary to ensure that incoming data meets required constraints before reaching the service layer.
 */
public interface UserService {

    /**
     * Creates a new user based on the provided UserRequest.
     *
     * @param userRequest the request object containing user details; must not be null and should contain required fields (e.g. name, email).
     * @throws IllegalArgumentException if the userRequest is invalid or if a user with the same email already exists.
     */
    void createUser(UserRequest userRequest) throws IllegalArgumentException;
    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve; must not be null.
     * @throws UserNotFoundException if the userId is null.
     */
    void updateUser(String userId, UserRequest userRequest) throws UserNotFoundException;
    /**
     * Deletes a user by their ID.
     *
     * @param userId the ID of the user to delete; must not be null.
     * @throws UserNotFoundException if the userId is null.
     */
    void deleteUser(String userId) throws UserNotFoundException;
    /**
     * Retrieves all users.
     *
     * @return a list of UserResponse objects containing all users; will be empty if no users are found.
     */
    List<UserResponse> getAllUsers();
    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve; must not be null.
     * @return the UserResponse object containing the user details; will be null if no user is found with the given ID.
     * @throws UserNotFoundException if the userId is null.
     */
    UserResponse getUserById(String userId) throws UserNotFoundException;
    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user to retrieve; must not be null and should be a valid email format.
     * @return the UserResponse object containing the user details; will be null if no user is found with the given email.
     * @throws UserNotFoundException if the email is null or not in a valid format.
     */
    UserResponse getUserByEmail(String email) throws UserNotFoundException;
    /**
     * Retrieves a list of users associated with a specific company ID.
     *
     * @param companyId the ID of the company whose users are to be retrieved; must not be null.
     * @return a list of UserResponse objects containing the details of users associated with the specified company; will be empty if no users are found for the given company ID.
     * @throws UserNotFoundException if the companyId is null.
     */
    List<UserResponse> getUsersByCompanyId(String companyId) throws UserNotFoundException;
}
