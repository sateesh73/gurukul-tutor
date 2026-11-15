package com.gurukul.authservice.security;

/**
 * Interface for authenticated principals that are scoped to a specific company/tenant.
 */
public interface CompanyScopedPrincipal {
    Long getCompanyId();
}
