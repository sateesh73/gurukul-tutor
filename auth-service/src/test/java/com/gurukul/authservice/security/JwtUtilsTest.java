package com.gurukul.authservice.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtUtils class with realistic Keycloak token contexts.
 */
@DisplayName("JWT Utils Tests with Actual Token Contexts")
class JwtUtilsTest {

    @Test
    @DisplayName("Should return false when security context has no authentication")
    void isAuthenticated() {
        SecurityContextHolder.clearContext();
        boolean result = JwtUtils.isAuthenticated();
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return null for current JWT when unauthenticated")
    void getCurrentJwtWhenNoAuth() {
        SecurityContextHolder.clearContext();
        Jwt result = JwtUtils.getCurrentJwt();
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should extract realm roles from Keycloak JWT")
    void getRealmRoles() {
        // Given - Real Keycloak claims
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("ADMIN", "TEACHER", "ASSOCIATE"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "f47ac10b-58cc-4372-a567-0e02b2c3d479");
        claims.put("preferred_username", "sateesh.kumar");
        claims.put("realm_access", realmAccess);

        Jwt mockJwt = new Jwt("real-keycloak-token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "RS256"), claims);

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockJwt);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);

        var securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(securityContext);

        // When
        List<String> result = JwtUtils.getRealmRoles();

        // Then
        assertThat(result).containsExactly("ADMIN", "TEACHER", "ASSOCIATE");
    }

    @Test
    @DisplayName("Should return null for current user ID when unauthenticated")
    void getCurrentUserId() {
        SecurityContextHolder.clearContext();
        String result = JwtUtils.getCurrentUserId();
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for current user ID as Long when unauthenticated")
    void getCurrentUserIdAsLong() {
        SecurityContextHolder.clearContext();
        Long result = JwtUtils.getCurrentUserIdAsLong();
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when claim does not exist or context is empty")
    void getClaimAsString() {
        SecurityContextHolder.clearContext();
        String result = JwtUtils.getClaimAsString("email");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return empty list for missing string list claims")
    void getClaimAsStringList() {
        SecurityContextHolder.clearContext();
        List<String> result = JwtUtils.getClaimAsStringList("roles");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return false for hasRole when unauthenticated")
    void hasRoleNoAuth() {
        SecurityContextHolder.clearContext();
        boolean result = JwtUtils.hasRole("ADMIN");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for hasAnyRole when unauthenticated")
    void hasAnyRole() {
        SecurityContextHolder.clearContext();
        boolean result = JwtUtils.hasAnyRole("ADMIN", "TEACHER", "STUDENT");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return empty list for authorities when unauthenticated")
    void getAuthoritiesAsString() {
        SecurityContextHolder.clearContext();
        List<String> result = JwtUtils.getAuthoritiesAsString();
        assertThat(result).isEmpty();
    }
}