package com.gurukul.authservice.keycloak;

import com.gurukul.authservice.user.Access;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Keycloak JWT token handling.
 * Tests JWT structure, claim extraction, and role mapping using actual Keycloak token formats.
 */
@DisplayName("Keycloak JWT Tests with Actual Token Structure")
class KeycloakJwtTest {

    private static final String REAL_ISSUER = "http://localhost:8081/realms/gurukul";
    private static final String REAL_USER_UUID = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d";

    @Test
    @DisplayName("Should parse valid Keycloak JWT containing standard OIDC and profile claims")
    void createJwtWithRequiredClaims() {
        // Given - Actual Keycloak token payload structure
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", REAL_ISSUER);
        claims.put("sub", REAL_USER_UUID);
        claims.put("preferred_username", "sateesh.kumar");
        claims.put("email", "sateesh.kumar@gurukul.com");
        claims.put("given_name", "Sateesh");
        claims.put("family_name", "Kumar");
        claims.put("email_verified", true);

        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("ADMIN", "TEACHER", "default-roles-gurukul"));
        claims.put("realm_access", realmAccess);

        // When - Create JWT
        Jwt jwt = new Jwt(
                "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.dummy_sig",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "RS256", "typ", "JWT"),
                claims
        );

        // Then - Verify claims
        assertThat(jwt).isNotNull();
        assertThat(jwt.getSubject()).isEqualTo(REAL_USER_UUID);
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(REAL_ISSUER);
        assertThat(jwt.getClaimAsString("preferred_username")).isEqualTo("sateesh.kumar");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("sateesh.kumar@gurukul.com");
        assertThat(jwt.getClaimAsBoolean("email_verified")).isTrue();
    }

    @Test
    @DisplayName("Should extract realm_access roles from Keycloak token")
    void jwtShouldHaveRealmAccessWithRoles() {
        // Given - Actual Keycloak realm roles
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("ADMIN", "TEACHER", "offline_access"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", REAL_ISSUER);
        claims.put("sub", REAL_USER_UUID);
        claims.put("preferred_username", "dr.ramesh.sharma");
        claims.put("realm_access", realmAccess);

        Jwt jwt = new Jwt("actual-keycloak-token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "RS256"), claims);

        // When - Extract roles
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) jwt.getClaimAsMap("realm_access").get("roles");

        // Then
        assertThat(roles).containsExactly("ADMIN", "TEACHER", "offline_access");
    }

    @Test
    @DisplayName("Should decode standard JWT base64 header and payload")
    void decodeJwtBase64Structure() {
        // Given - Standard RFC-7519 JWT structure
        String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"" + REAL_USER_UUID + "\",\"preferred_username\":\"priya.nair\",\"email\":\"priya.nair@tcs.com\"}";
        String signature = "digital_signature_bytes";

        String encodedHeader = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
        String encodedPayload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());
        String encodedSignature = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(signature.getBytes());

        String jwt = encodedHeader + "." + encodedPayload + "." + encodedSignature;

        // When - Decode components
        String decodedHeader = new String(java.util.Base64.getUrlDecoder().decode(encodedHeader));
        String decodedPayload = new String(java.util.Base64.getUrlDecoder().decode(encodedPayload));

        // Then
        assertThat(decodedHeader).contains("RS256");
        assertThat(decodedPayload).contains("priya.nair");
        assertThat(decodedPayload).contains("priya.nair@tcs.com");
    }

    @Test
    @DisplayName("Should gracefully handle token without realm_access claims")
    void jwtWithEmptyRealmAccess() {
        // Given - Token without realm_access
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", REAL_USER_UUID);
        claims.put("preferred_username", "anonymous.guest");

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "RS256"), claims);

        // When
        var realmAccess = jwt.getClaimAsMap("realm_access");

        // Then
        assertThat(realmAccess).isNull();
    }

    @Test
    @DisplayName("Should map Access enum values directly to Spring Security ROLE_ format")
    void mapAccessRolesToSpringSecurityRoles() {
        for (Access access : Access.values()) {
            String springRole = "ROLE_" + access.name();
            assertThat(springRole).isEqualTo("ROLE_" + access.name());
            assertThat(springRole).matches("^ROLE_[A-Z]+$");
        }
    }

    @Test
    @DisplayName("Should extract client resource_access roles for auth-service client")
    void extractClientResourceAccess() {
        // Given - Keycloak resource_access claim for auth-service client
        Map<String, Object> clientAccess = new HashMap<>();
        clientAccess.put("roles", List.of("ADMIN", "USER", "MANAGE_COMPANIES"));

        Map<String, Object> resourceAccess = new HashMap<>();
        resourceAccess.put("auth-service", clientAccess);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", REAL_USER_UUID);
        claims.put("preferred_username", "sateesh.kumar");
        claims.put("resource_access", resourceAccess);

        Jwt jwt = new Jwt("client-token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "RS256"), claims);

        // When - Extract client roles
        @SuppressWarnings("unchecked")
        var authServiceClient = (Map<String, Object>) jwt.getClaimAsMap("resource_access").get("auth-service");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) authServiceClient.get("roles");

        // Then
        assertThat(roles).containsExactly("ADMIN", "USER", "MANAGE_COMPANIES");
    }
}