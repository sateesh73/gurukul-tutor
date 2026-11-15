package com.gurukul.authservice.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtAuthenticationConverter class using realistic Keycloak token payloads.
 */
@DisplayName("JWT Authentication Converter Tests with Actual Keycloak Tokens")
class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    private static final String REAL_USER_UUID = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    @Test
    @DisplayName("Should convert Keycloak JWT with realm roles and map preferred_username to principal")
    void convertJwtWithRealmRoles() {
        // Given - Real Keycloak claims
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("ADMIN", "TEACHER", "default-roles-gurukul"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "http://localhost:8081/realms/gurukul");
        claims.put("sub", REAL_USER_UUID);
        claims.put("preferred_username", "sateesh.kumar");
        claims.put("email", "sateesh.kumar@gurukul.com");
        claims.put("realm_access", realmAccess);

        Jwt jwt = new Jwt("keycloak-access-token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "RS256"), claims);

        // When
        var result = converter.convert(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("sateesh.kumar");
        assertThat(result.getAuthorities())
                .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "ROLE_TEACHER", "ROLE_DEFAULT-ROLES-GURUKUL");
    }

    @Test
    @DisplayName("Should convert Keycloak JWT with client resource_access roles")
    void convertJwtWithClientRoles() {
        // Given - Keycloak resource_access for auth-service client
        Map<String, Object> clientRoles = new HashMap<>();
        clientRoles.put("roles", List.of("ADMIN", "USER"));

        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("auth-service", clientRoles);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", REAL_USER_UUID);
        claims.put("preferred_username", "dr.ramesh.sharma");
        claims.put("resource_access", resourceAccess);

        Jwt jwt = new Jwt("keycloak-client-token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "RS256"), claims);

        // When
        var result = converter.convert(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("dr.ramesh.sharma");
        assertThat(result.getAuthorities())
                .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @DisplayName("Should handle Keycloak token with no explicit role assignments")
    void convertJwtWithEmptyRoles() {
        // Given - Token without roles
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", REAL_USER_UUID);
        claims.put("preferred_username", "unassigned.guest");

        Jwt jwt = new Jwt("guest-token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "RS256"), claims);

        // When
        var result = converter.convert(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("unassigned.guest");
        assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Should return null when passed null JWT")
    void convertNullJwt() {
        // When
        var result = converter.convert(null);

        // Then
        assertThat(result).isNull();
    }
}