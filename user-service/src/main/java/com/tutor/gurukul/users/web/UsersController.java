package com.tutor.gurukul.users.web;

import com.tutor.gurukul.users.UserService;
import com.tutor.gurukul.users.exception.UserNotFoundException;
import com.tutor.gurukul.users.model.UserRequest;
import com.tutor.gurukul.users.model.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing users.
 * Provides endpoints to create, read, update, and delete user information.
 * This controller delegates business logic to the {@link UserService} and is responsible for handling HTTP requests and responses.
 * The controller methods are designed to follow RESTful conventions, using appropriate HTTP methods and status codes for each operation.
 * Recommended enhancements include:
 * - Adding validation annotations (e.g. @Valid) to request bodies to ensure that incoming data meets required constraints before being processed by the service layer.
 * - Implementing exception handling (e.g. @ControllerAdvice) to translate service exceptions into meaningful API error responses.
 * - Adding logging to track incoming requests and any errors that occur during processing.
 * - Implementing pagination for the getAllUsers endpoint to handle large datasets efficiently.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
class UsersController {

    /** Service used to perform business logic related to user management.
     *
     * <p>This field is injected via Lombok's {@code @RequiredArgsConstructor} (constructor injection).
     * The concrete {@code UserService} implementation is expected to provide methods for creating, retrieving, updating, and deleting users.
     * The controller delegates all business logic to this service and should not contain any direct data access or complex processing logic.
     */
    private final UserService userService;

    /**
     * Retrieves all users.
     *
     * @return a ResponseEntity containing a list of UserResponse objects with HTTP status 200 OK; the list will be empty if no users are found.
     */
    @GetMapping
    ResponseEntity<List<UserResponse>> getAllUsers() {
        var users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve; must not be null.
     * @return a ResponseEntity containing the UserResponse with HTTP status 200 OK if found, or an appropriate error status if not found or if the request is invalid.
     */
    @GetMapping("/{userId}")
    ResponseEntity<UserResponse> getUserById(String userId) throws UserNotFoundException {
        var user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user to retrieve; must not be null and should be a valid email format.
     * @return a ResponseEntity containing the UserResponse with HTTP status 200 OK if found, or an appropriate error status if not found or if the request is invalid.
     */
    @GetMapping("/email/{email}")
    ResponseEntity<UserResponse> getUserByEmail(String email) throws UserNotFoundException {
        var user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves a list of users associated with a specific company ID.
     *
     * @param companyId the ID of the company whose users are to be retrieved; must not be null.
     * @return a ResponseEntity containing a list of UserResponse objects with HTTP status 200 OK; the list will be empty if no users are found for the given company ID, or an appropriate error status if the request is invalid.
     */
    @GetMapping("/company/{companyId}")
    ResponseEntity<List<UserResponse>> getUsersByCompanyId(String companyId) throws UserNotFoundException {
        var users = userService.getUsersByCompanyId(companyId);
        return ResponseEntity.ok(users);
    }

    /**
     * Creates a new user based on the provided UserRequest.
     *
     * @param userRequest the request body containing user details; must not be null and should contain required fields (e.g. name, email).
     * @return a ResponseEntity with HTTP status 201 Created and a Location header pointing to the newly created resource; returns an appropriate error status if the request is invalid.
     */
    @PostMapping
    ResponseEntity<Void> createUser(@RequestBody UserRequest userRequest) {
        userService.createUser(userRequest);
        var location = "/api/v1/users/email/" + userRequest.email();
        return ResponseEntity.created(URI.create(location)).build();
    }

    /**
     * Updates an existing user with the provided details.
     *
     * @param userId the ID of the user to update; must not be null.
     * @param userRequest the request body containing updated user details; must not be null and should contain required fields (e.g. name, email).
     * @return a ResponseEntity with HTTP status 204 No Content if the update is successful; returns an appropriate error status if the request is invalid or if the user is not found.
     */
    @PutMapping
    ResponseEntity<Void> updateUser(String userId, @RequestBody UserRequest userRequest) throws UserNotFoundException {
        userService.updateUser(userId, userRequest);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId the ID of the user to delete; must not be null.
     * @return a ResponseEntity with HTTP status 204 No Content if the deletion is successful; returns an appropriate error status if the request is invalid or if the user is not found.
     */
    @DeleteMapping
    ResponseEntity<Void> deleteUser(String userId) throws UserNotFoundException {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
