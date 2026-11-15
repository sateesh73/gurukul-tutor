package com.gurukul.authservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

/**
 * Converts JWT tokens to Spring Security Authentication objects.
 * Handles Keycloak-specific claim structures for role extraction.
 */
@Slf4j
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        log.debug("Converting JWT for subject: {}", jwt.getSubject());

        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);

        String principal = jwt.getClaimAsString("preferred_username");
        if (principal == null || principal.isBlank()) {
            principal = jwt.getSubject();
        }

        return new JwtAuthenticationToken(
                principal,
                jwt,
                authorities,
                jwt.getClaims()
        );
    }

    private Collection<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();

        // 1. Try 'roles' claim (custom claim or Keycloak mapper)
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            for (String role : roles) {
                authorities.add(toAuthority(role));
            }
        }

        // 2. Try 'authorities' claim as fallback
        List<String> claimAuthorities = jwt.getClaimAsStringList("authorities");
        if (claimAuthorities != null) {
            for (String auth : claimAuthorities) {
                authorities.add(toAuthority(auth));
            }
        }

        // 3. Try 'claims' claim (for tests)
        List<String> customClaims = jwt.getClaimAsStringList("claims");
        if (customClaims != null) {
            for (String auth : customClaims) {
                authorities.add(toAuthority(auth));
            }
        }

        // 4. Keycloak realm roles: realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            Object realmRoles = realmAccess.get("roles");
            if (realmRoles instanceof Collection<?> roleList) {
                for (Object role : roleList) {
                    if (role != null) {
                        authorities.add(toAuthority(role.toString()));
                    }
                }
            }
        }

        // 5. Keycloak client roles: resource_access.{client-name}.roles
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            for (var entry : resourceAccess.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Map<?, ?> clientMap) {
                    Object clientRoles = clientMap.get("roles");
                    if (clientRoles instanceof Collection<?> roleList) {
                        for (Object role : roleList) {
                            if (role != null) {
                                authorities.add(toAuthority(role.toString()));
                            }
                        }
                    }
                } else if (value instanceof Collection<?> roleList) {
                    for (Object role : roleList) {
                        if (role != null) {
                            authorities.add(toAuthority(role.toString()));
                        }
                    }
                }
            }
        }

        return new ArrayList<>(authorities);
    }

    private SimpleGrantedAuthority toAuthority(String role) {
        if (role == null || role.isBlank()) {
            return new SimpleGrantedAuthority("ROLE_USER");
        }
        if (role.startsWith("ROLE_")) {
            return new SimpleGrantedAuthority(role.toUpperCase());
        }
        return new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
    }
}