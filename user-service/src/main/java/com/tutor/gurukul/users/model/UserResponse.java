package com.tutor.gurukul.users.model;

import lombok.Builder;

/*
 * This record represents the response object for a user in the system.
 * It contains all the necessary information about a user, including their personal details, contact information, and role within the system.
 * The fields included in this record are:
 * - userId: A unique identifier for the user.
 * - firstName: The user's first name.
 * - lastName: The user's last name.
 * - email: The user's email address.
 * - companyId: The identifier of the company the user is associated with.
 * - phoneNumber: The user's phone number.
 * - role: The role of the user within the system (e.g., STUDENT, TUTOR, ADMIN).
 * - street: The street address of the user.
 * - city: The city where the user resides.
 * - state: The state where the user resides.
 * - zipCode: The postal code for the user's address.
 * - country: The country where the user resides.
 */
@Builder
public record UserResponse(
        String userId,
        String firstName,
        String lastName,
        String email,
        String companyId,
        String phoneNumber,
        String role,
        String street,
        String city,
        String state,
        String zipCode,
        String country
) {
}
