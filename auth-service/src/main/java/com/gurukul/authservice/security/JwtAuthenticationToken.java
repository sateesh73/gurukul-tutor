package com.gurukul.authservice.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Custom AuthenticationToken for JWT-based authentication.
 * Implements UserDetails for easy access to user information.
 */
@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken implements UserDetails {

    private final String principal;
    private final Object credentials;
    private final Jwt jwt;
    private final Map<String, Object> jwtClaims;
    private final Collection<? extends GrantedAuthority> authoritiesCache;

    public JwtAuthenticationToken(String principal, Object credentials,
                                  Collection<? extends GrantedAuthority> authorities,
                                  Map<String, Object> jwtClaims) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.jwt = credentials instanceof Jwt j ? j : null;
        this.jwtClaims = jwtClaims != null ? jwtClaims : (jwt != null ? jwt.getClaims() : Collections.emptyMap());
        this.authoritiesCache = authorities != null ? authorities : Collections.emptyList();
        setAuthenticated(true);
    }

    @Override
    public String getPassword() {
        return null; // JWT token, password is in the token
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }
}