package com.gurukul.authservice.company;

/**
 * Domain event published when a new company is created.
 * Used to automatically provision an initial company admin user in the user module
 * without introducing circular dependencies or violating package encapsulation.
 */
public record CompanyCreatedEvent(
    Long companyId,
    String companyName,
    String companyCode,
    String adminEmail,
    String adminPassword
) {}
