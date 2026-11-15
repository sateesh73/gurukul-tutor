package com.gurukul.authservice.security;

import com.gurukul.authservice.dto.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Access denied handler for JWT-based authorization.
 * Handles requests where authenticated users lack required permissions.
 * Package-protected.
 */
@Slf4j
class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAccessDeniedHandler() {
        // Default constructor for test configuration
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                         AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.debug("Access denied: {}", accessDeniedException.getMessage());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN",
                "You do not have permission to access this resource."
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}