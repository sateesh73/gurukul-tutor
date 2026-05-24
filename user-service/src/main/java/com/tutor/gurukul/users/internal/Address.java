package com.tutor.gurukul.users.internal;

import lombok.Builder;

/**
 * Represents an address with street, city, state, zip code, and country.
 * This record is used as an embedded object within the Users entity to store address information.
 * It provides a convenient way to encapsulate address-related fields and can be easily reused across different entities if needed.
 * The @Builder annotation allows for easy construction of instances using a fluent API.
 * Recommended enhancements include adding validation annotations (e.g. @NotNull) to ensure that required fields are provided when creating an instance of Address.
 */
@Builder
record Address(
        String street,
        String city,
        String state,
        String zipCode,
        String country
) {
}
