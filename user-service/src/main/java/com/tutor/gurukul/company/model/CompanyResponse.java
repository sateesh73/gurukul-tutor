package com.tutor.gurukul.company.model;

import lombok.Builder;
/**
 * CompanyResponse is a record that represents the response object for a company.
 * It contains the following fields:
 * - id: The unique identifier of the company.
 * - name: The name of the company.
 * - email: The email address of the company.
 * - description: A brief description of the company.
 * - websiteUrl: The URL of the company's website.
 * - logoUrl: The URL of the company's logo.
 *
 * This record is used to transfer data between different layers of the application,
 * such as from the service layer to the controller layer, or from the controller
 * layer to the client.
 */
@Builder
public record CompanyResponse(
        String id,
        String name,
        String email,
        String description,
        String websiteUrl,
        String logoUrl
) {
}
