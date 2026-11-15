package com.gurukul.authservice.user;

import com.gurukul.authservice.security.CompanyScopedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserPrincipal - Spring Security UserDetails implementation that wraps the User entity.
 * Maps Access enum values to Spring Security roles (ROLE_ prefix added).
 * Package-protected.
 */
class UserPrincipal implements UserDetails, CompanyScopedPrincipal {

    private final Long id;
    private final String username;
    private final String email;
    private final String password;
    private final Long companyId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    UserPrincipal(Long id, String username, String email, String password,
                  Long companyId, Collection<? extends GrantedAuthority> authorities, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.companyId = companyId;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getCompanyId(),
                mapAccessToAuthorities(user.getAccess()),
                user.getEnabled()
        );
    }

    private static Collection<? extends GrantedAuthority> mapAccessToAuthorities(Access access) {
        if (access == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority(access.getRoleName()));
    }

    List<String> getRealmRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    // Getters
    Long getId() {
        return id;
    }

    @Override
    public Long getCompanyId() {
        return companyId;
    }

    String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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
        return enabled;
    }
}