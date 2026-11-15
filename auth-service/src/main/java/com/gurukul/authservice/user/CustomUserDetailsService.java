package com.gurukul.authservice.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Custom UserDetailsService that loads users from the local database
 * and maps Access enum values to Spring Security roles.
 * Delegates to UserService for user data access.
 * Package-protected.
 */
@Slf4j
@RequiredArgsConstructor
class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user from database: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return UserPrincipal.from(user);
    }

    /**
     * Loads user by ID.
     */
    UserDetails loadUserById(Long userId) {
        log.debug("Loading user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        return UserPrincipal.from(user);
    }
}