package com.tutor.gurukul.users.exception;

import java.nio.file.attribute.UserPrincipalNotFoundException;

/**
 * Exception thrown when a user is not found in the system.
 * This exception indicates that a user with the specified ID or email does not exist in the database, preventing retrieval, update, or deletion operations on non-existent user records.
 * It extends UserPrincipalNotFoundException, which is a standard exception in Java for indicating that a user principal was not found.
 * This exception can be used in the UserService implementation to signal that an operation failed due to the absence of a user with the specified identifier.
 */
public class UserNotFoundException extends UserPrincipalNotFoundException {
    /**
     * Constructs an instance of this class.
     *
     * @param name the principal name; may be {@code null}
     */
    public UserNotFoundException(String name) {
        super(name);
    }
}
