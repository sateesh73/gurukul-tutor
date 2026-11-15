package com.gurukul.authservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides access to the current authenticated user's context.
 * Works with both UserPrincipal (local DB) and Jwt (OAuth2).
 * Uses Access enum values from the database User entity for authorization.
 */
@Slf4j
public class UserContext {

    /**
     * Get the current user's username.
     */
    public static String getCurrentUsername() {
        Authentication auth = getAuthentication();
        if (auth == null) return null;
        String preferredUsername = JwtUtils.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }
        return auth.getName();
    }

    /**
     * Get the current user's ID as Long.
     */
    public static Long getCurrentUserId() {
        String id = getCurrentUserIdAsString();
        if (id != null) {
            try {
                return Long.valueOf(id);
            } catch (NumberFormatException e) {
                log.debug("Could not parse user ID as Long: {}", id);
            }
        }
        return null;
    }

    /**
     * Get the current user's ID as String.
     */
    public static String getCurrentUserIdAsString() {
        Authentication auth = getAuthentication();
        if (auth != null) {
            return auth.getName();
        }
        return null;
    }

    /**
     * Check if a user is currently authenticated.
     */
    public static boolean isAuthenticated() {
        return JwtUtils.isAuthenticated();
    }

    /**
     * Get the current user's email.
     */
    public static String getEmail() {
        return JwtUtils.getEmail();
    }

    /**
     * Get the current user's company ID from principal or claims.
     */
    public static Long getCurrentCompanyId() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CompanyScopedPrincipal csp) {
            return csp.getCompanyId();
        }
        String companyIdStr = JwtUtils.getClaimAsString("company_id");
        if (companyIdStr == null) {
            companyIdStr = JwtUtils.getClaimAsString("companyId");
        }
        if (companyIdStr != null) {
            try {
                return Long.valueOf(companyIdStr);
            } catch (NumberFormatException e) {
                log.debug("Could not parse company ID as Long: {}", companyIdStr);
            }
        }
        return null;
    }

    /**
     * Get the current user's Access level/role as String.
     * Maps from User entity's Access enum roles.
     */
    public static String getAccessLevel() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            // Get role from authorities (mapped from Access enum)
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(a -> a.replace("ROLE_", ""))
                    .findFirst()
                    .orElse("TEMP");
        }
        return "TEMP";
    }

    /**
     * Check if current user has a specific Access role.
     */
    public static boolean hasAccess(String access) {
        return JwtUtils.hasRole(access);
    }

    /**
     * Check if current user has a specific role (alias for hasAccess).
     */
    public static boolean hasRole(String role) {
        return JwtUtils.hasRole(role);
    }

    /**
     * Check if current user has any of the specified roles.
     */
    public static boolean hasAnyRole(String... roles) {
        return JwtUtils.hasAnyRole(roles);
    }

    /**
     * Check if current user has any of the specified Access roles.
     */
    public static boolean hasAnyAccess(String... accesses) {
        return List.of(accesses).stream().anyMatch(UserContext::hasAccess);
    }

    /**
     * Check if current user has ADMIN access.
     */
    public static boolean isAdmin() {
        return hasAccess("ADMIN");
    }

    /**
     * Check if current user has TEACHER access.
     */
    public static boolean isTeacher() {
        return hasAccess("TEACHER");
    }

    /**
     * Check if current user has STUDENT access.
     */
    public static boolean isStudent() {
        return hasAccess("STUDENT");
    }

    /**
     * Check if current user has ASSOCIATE access.
     */
    public static boolean isAssociate() {
        return hasAccess("ASSOCIATE");
    }

    /**
     * Check if current user has TEMP access.
     */
    public static boolean isTemp() {
        return hasAccess("TEMP");
    }

    /**
     * Check if user is enabled/active.
     */
    public static boolean isEnabled() {
        Authentication auth = getAuthentication();
        if (auth != null) {
            // Check if enabled attribute is present in JWT or User
            Boolean enabled = (Boolean) auth.getDetails();
            if (enabled != null) return enabled;
        }
        return JwtUtils.isAuthenticated();
    }

    /**
     * Get the current authentication object.
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Get authority names as list of strings.
     */
    public static List<String> getAuthorityNames() {
        Authentication auth = getAuthentication();
        if (auth == null) return List.of();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}