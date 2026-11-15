package com.gurukul.authservice.web;

import com.gurukul.authservice.dto.UserDto;
import com.gurukul.authservice.exception.ResourceNotFoundException;
import com.gurukul.authservice.security.UserContext;
import com.gurukul.authservice.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            Authentication authentication) {
        log.debug("GET /api/v1/auth/me called. headerUserId={}, authentication={}", headerUserId, authentication);

        // 1. If explicit gateway header is provided
        if (headerUserId != null) {
            return ResponseEntity.ok(userService.findById(headerUserId));
        }

        // 2. If authenticated via Spring Security / Keycloak JWT
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            if (username != null && userService.existsByUsername(username)) {
                return ResponseEntity.ok(userService.findByUsername(username));
            }

            // Check preferred_username claim from JWT
            String preferredUsername = UserContext.getCurrentUsername();
            if (preferredUsername != null && userService.existsByUsername(preferredUsername)) {
                return ResponseEntity.ok(userService.findByUsername(preferredUsername));
            }

            // Check email claim from JWT
            String email = UserContext.getEmail();
            if (email != null) {
                try {
                    return ResponseEntity.ok(userService.findByUsername(email));
                } catch (ResourceNotFoundException ignored) {
                }
            }
        }

        throw new ResourceNotFoundException("Authenticated user details not found in database. Please ensure user account exists.");
    }
}