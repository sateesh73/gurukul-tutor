package com.tutor.gurukul.users.model;

import lombok.Builder;

/**
 * UserRequest is a record that represents the request object for a user.
 * It contains the following fields:
 * - companyId: The identifier of the company the user is associated with.
 * - firstName: The first name of the user.
 * - lastName: The last name of the user.
 * - email: The email address of the user.
 * - password: The password of the user.
 * - phoneNumber: The phone number of the user.
 * - role: The role of the user (e.g., "STUDENT", "TUTOR", "ADMIN").
 * - street: The street address of the user.
 * - city: The city of the user's address.
 * - state: The state of the user's address.
 * - zipCode: The zip code of the user's address.
 * - country: The country of the user's address.
 *
 * This record is used to transfer data between different layers of the application,
 * such as from the controller layer to the service layer, or from the client
 * to the controller layer.
 */
@Builder
public record UserRequest(
        String companyId,
        String firstName,
        String lastName,
        String email,
        String password,
        String phoneNumber,
        String role,
        String street,
        String city,
        String state,
        String zipCode,
        String country

) {
}
