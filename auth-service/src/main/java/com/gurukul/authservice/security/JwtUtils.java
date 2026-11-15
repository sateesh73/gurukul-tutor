package com.gurukul.authservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for JWT token operations and user context utilities.
 * Provides methods to extract claims and user information from authenticated requests.
 * Works with both OAuth2 JWT and local database authenticated users.
 */
@Slf4j
public class JwtUtils {

    /**
     * Get the current authenticated JWT from the security context.
     */
    public static Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken springJwt) {
            return springJwt.getToken();
        }
        if (authentication instanceof JwtAuthenticationToken customJwt && customJwt.getJwt() != null) {
            return customJwt.getJwt();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Map<String, Object> claims = jwtAuth.getJwtClaims();
            return new Jwt(
                    authentication.getName() != null ? authentication.getName() : "user",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    claims != null && !claims.isEmpty() ? claims : Map.of("sub", authentication.getName() != null ? authentication.getName() : "user")
            );
        }
        return null;
    }

    /**
     * Get the user ID (sub claim) from the JWT or authentication name.
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Get the user ID as Long from authentication.
     */
    public static Long getCurrentUserIdAsLong() {
        String id = getCurrentUserId();
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
     * Get the user's email from JWT claims.
     */
    public static String getEmail() {
        return getClaimAsString("email");
    }

    /**
     * Get a claim value as String.
     */
    public static String getClaimAsString(String claimName) {
        Map<String, Object> claims = getClaims();
        if (claims.isEmpty()) return null;
        Object value = claims.get(claimName);
        return value != null ? value.toString() : null;
    }

    /**
     * Get a claim value as List of Strings.
     */
    public static List<String> getClaimAsStringList(String claimName) {
        Map<String, Object> claims = getClaims();
        if (claims.isEmpty()) return Collections.emptyList();

        Object value = claims.get(claimName);
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Get realm roles from the JWT.
     */
    public static List<String> getRealmRoles() {
        Map<String, Object> claims = getClaims();
        if (claims.isEmpty()) return Collections.emptyList();

        Object realmAccessObj = claims.get("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccess) {
            Object roles = realmAccess.get("roles");
            if (roles instanceof List<?> roleList) {
                return roleList.stream().map(Object::toString).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get all authorities from the current authentication.
     */
    public static List<String> getAuthoritiesAsString() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Collections.emptyList();

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    /**
     * Check if the user has a specific role.
     * Works with both JWT-based and UserPrincipal-based authentication.
     */
    public static boolean hasRole(String role) {
        if (role == null || role.isBlank()) return false;
        String rolePrefix = "ROLE_";
        String targetRole = role.startsWith(rolePrefix) ? role : rolePrefix + role.toUpperCase();
        String simpleRole = role.replace(rolePrefix, "");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            boolean hasRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equalsIgnoreCase(targetRole) || auth.equalsIgnoreCase(role));

            if (hasRole) return true;
        }

        // Fallback to JWT roles
        List<String> roles = getRealmRoles();
        if (roles.isEmpty()) {
            roles = getClaimAsStringList("roles");
        }
        return roles.stream()
                .anyMatch(r -> r.equalsIgnoreCase(simpleRole) || r.equalsIgnoreCase(role) || ("ROLE_" + r.toUpperCase()).equalsIgnoreCase(targetRole));
    }

    /**
     * Check if the user has any of the specified roles.
     */
    public static boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the user is authenticated.
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }

    /**
     * Get claims map from the current JWT.
     */
    private static Map<String, Object> getClaims() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Collections.emptyMap();
        }
        if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken springJwt) {
            return springJwt.getTokenAttributes();
        }
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getJwtClaims() != null ? jwtToken.getJwtClaims() : Collections.emptyMap();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaims();
        }
        return Collections.emptyMap();
    }
}