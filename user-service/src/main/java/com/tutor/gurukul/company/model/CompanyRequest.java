package com.tutor.gurukul.company.model;

import lombok.Builder;

/**
 * Request object for creating or updating a company.
 * This record encapsulates the necessary information for a company, including its name, email, description, website URL, and logo URL.
 * The @Builder annotation allows for easy construction of instances using a fluent API.
 * This class is used as a data transfer object (DTO) in the service layer and controllers to receive input from clients when creating or updating company information.
 * Recommended enhancements include adding validation annotations (e.g. @NotNull, @Email) to ensure that incoming data meets required constraints before being processed by the service layer.
 */
@Builder
public record CompanyRequest(
        String name,
        String email,
        String description,
        String websiteUrl,
        String logoUrl
) {
}
