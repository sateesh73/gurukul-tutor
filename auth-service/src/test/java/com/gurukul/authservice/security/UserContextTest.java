package com.gurukul.authservice.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserContext class with actual authority representations.
 */
@DisplayName("User Context Tests with Realistic Authorities")
class UserContextTest {

    @Test
    @DisplayName("Should return false when user is not authenticated")
    void isAuthenticated() {
        SecurityContextHolder.clearContext();
        boolean result = UserContext.isAuthenticated();
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return null for current username when unauthenticated")
    void getCurrentUsername() {
        SecurityContextHolder.clearContext();
        String result = UserContext.getCurrentUsername();
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for current user ID when unauthenticated")
    void getCurrentUserId() {
        SecurityContextHolder.clearContext();
        Long result = UserContext.getCurrentUserId();
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return false for isAdmin when unauthenticated")
    void isAdmin() {
        SecurityContextHolder.clearContext();
        boolean result = UserContext.isAdmin();
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for isTeacher when unauthenticated")
    void isTeacher() {
        SecurityContextHolder.clearContext();
        boolean result = UserContext.isTeacher();
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for isStudent when unauthenticated")
    void isStudent() {
        SecurityContextHolder.clearContext();
        boolean result = UserContext.isStudent();
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for isAssociate when unauthenticated")
    void isAssociate() {
        SecurityContextHolder.clearContext();
        boolean result = UserContext.isAssociate();
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true for isAdmin when user has ROLE_ADMIN authority")
    void isAdminWhenHasAdminRole() {
        createMockAuthenticationWithRoles("ROLE_ADMIN");
        boolean result = UserContext.isAdmin();
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false for isAdmin when user has ROLE_STUDENT authority")
    void isAdminWhenNotHasAdminRole() {
        createMockAuthenticationWithRoles("ROLE_STUDENT");
        boolean result = UserContext.isAdmin();
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true for isTeacher when user has ROLE_TEACHER authority")
    void isTeacherWhenHasTeacherRole() {
        createMockAuthenticationWithRoles("ROLE_TEACHER");
        boolean result = UserContext.isTeacher();
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true for isStudent when user has ROLE_STUDENT authority")
    void isStudentWhenHasStudentRole() {
        createMockAuthenticationWithRoles("ROLE_STUDENT");
        boolean result = UserContext.isStudent();
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true for isAssociate when user has ROLE_ASSOCIATE authority")
    void isAssociateWhenHasAssociateRole() {
        createMockAuthenticationWithRoles("ROLE_ASSOCIATE");
        boolean result = UserContext.isAssociate();
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should check hasAccess method against specific role")
    void hasAccess() {
        SecurityContextHolder.clearContext();
        boolean result = UserContext.hasAccess("ADMIN");
        assertThat(result).isFalse();

        createMockAuthenticationWithRoles("ROLE_ADMIN");
        assertThat(UserContext.hasAccess("ADMIN")).isTrue();
        assertThat(UserContext.hasAccess("TEACHER")).isFalse();
    }

    @Test
    @DisplayName("Should check hasAnyAccess method against multiple roles")
    void hasAnyAccess() {
        SecurityContextHolder.clearContext();
        boolean result = UserContext.hasAnyAccess("ADMIN", "TEACHER");
        assertThat(result).isFalse();

        createMockAuthenticationWithRoles("ROLE_TEACHER");
        assertThat(UserContext.hasAnyAccess("ADMIN", "TEACHER")).isTrue();
        assertThat(UserContext.hasAnyAccess("ADMIN", "STUDENT")).isFalse();
    }

    @Test
    @DisplayName("Should get highest access level from authorities")
    void getAccessLevel() {
        SecurityContextHolder.clearContext();
        assertThat(UserContext.getAccessLevel()).isEqualTo("TEMP");

        createMockAuthenticationWithRoles("ROLE_ADMIN");
        assertThat(UserContext.getAccessLevel()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Should return null for email when unauthenticated")
    void getEmail() {
        SecurityContextHolder.clearContext();
        String result = UserContext.getEmail();
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for company ID when unauthenticated")
    void getCurrentCompanyId() {
        SecurityContextHolder.clearContext();
        Long result = UserContext.getCurrentCompanyId();
        assertThat(result).isNull();
    }

    @SuppressWarnings("unchecked")
    private void createMockAuthenticationWithRoles(String... roles) {
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("sateesh.kumar");
        when(mockAuth.isAuthenticated()).thenReturn(true);

        var authorities = java.util.List.of(roles);
        var grantedAuthorities = authorities.stream()
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .collect(java.util.stream.Collectors.toList());

        when(mockAuth.getAuthorities()).thenReturn((Collection) grantedAuthorities);

        var securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(mockAuth);
        SecurityContextHolder.setContext(securityContext);
    }
}