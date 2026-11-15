package com.gurukul.authservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test configuration for security.
 * Provides a mock JwtDecoder for tests when no real Keycloak is available.
 */
@TestConfiguration(proxyBeanMethods = false)
@EnableWebSecurity
class TestSecurityConfiguration {

    /**
     * Mock JwtDecoder that accepts any token for tests.
     */
    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        // Create a decoder that accepts any JWT
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) {
                // Create a minimal valid JWT
                Map<String, Object> claims = new HashMap<>();
                claims.put("sub", "test-user");
                claims.put("email", "test@example.com");
                claims.put("roles", List.of("ADMIN"));

                return new Jwt(token, java.time.Instant.now(),
                    java.time.Instant.now().plusSeconds(3600),
                    Map.of("alg", "none"),
                    claims
                );
            }
        };
    }

    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/users/**").permitAll()
                .requestMatchers("/api/v1/companies/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new TestJwtAuthenticationEntryPoint())
                .accessDeniedHandler(new TestJwtAccessDeniedHandler())
            );
        return http.build();
    }

    // Test implementations with no-arg constructors
    static class TestJwtAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void commence(jakarta.servlet.http.HttpServletRequest request,
                            jakarta.servlet.http.HttpServletResponse response,
                            org.springframework.security.core.AuthenticationException authException)
                throws java.io.IOException, jakarta.servlet.ServletException {
            response.setContentType("application/json");
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);

            var errorResponse = new com.gurukul.authservice.dto.ApiErrorResponse(
                jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication required"
            );
            response.getWriter().write(mapper.writeValueAsString(errorResponse));
        }
    }

    static class TestJwtAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void handle(jakarta.servlet.http.HttpServletRequest request,
                          jakarta.servlet.http.HttpServletResponse response,
                          org.springframework.security.access.AccessDeniedException accessDeniedException)
                throws java.io.IOException, jakarta.servlet.ServletException {
            response.setContentType("application/json");
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);

            var errorResponse = new com.gurukul.authservice.dto.ApiErrorResponse(
                jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN,
                "FORBIDDEN",
                "Access denied"
            );
            response.getWriter().write(mapper.writeValueAsString(errorResponse));
        }
    }
}