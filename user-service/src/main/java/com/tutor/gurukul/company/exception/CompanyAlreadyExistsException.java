package com.tutor.gurukul.company.exception;

/**
 * Exception thrown when attempting to create a user that already exists.
 * This exception indicates that a user with the same email or unique identifier already exists in the system, preventing the creation of duplicate user records.
 * It extends RuntimeException, allowing it to be thrown without being declared in method signatures.
 * This exception can be used in the UserService implementation to signal that a user creation operation failed due to an existing user with the same email or identifier.
 */
public class CompanyAlreadyExistsException extends  RuntimeException{

    /**
     * Constructs an instance of this class.
     *
     * @param name the principal name; may be {@code null}
     */
    public CompanyAlreadyExistsException(String name) {
        super(name);
    }
}
