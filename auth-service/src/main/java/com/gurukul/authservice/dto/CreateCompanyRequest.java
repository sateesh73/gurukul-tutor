package com.gurukul.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
    @NotBlank(message = "Company name cannot be blank")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    String name,

    @NotBlank(message = "Company code cannot be blank")
    @Size(min = 2, max = 50, message = "Company code must be between 2 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Company code must contain only alphanumeric characters, underscores, or hyphens")
    String code,

    String adminEmail,
    String adminPassword
) {
    public CreateCompanyRequest(String name, String code) {
        this(name, code, null, null);
    }
}