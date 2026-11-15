package com.gurukul.authservice.user;

import com.gurukul.authservice.company.CompanyCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Listens for CompanyCreatedEvent and automatically provisions an initial ADMIN user
 * with the same name as the company and access level ADMIN.
 * Package-protected.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class CompanyUserEventListener {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener
    public void onCompanyCreated(CompanyCreatedEvent event) {
        log.info("Handling CompanyCreatedEvent: provisioning default admin for company '{}' (id: {})",
                event.companyName(), event.companyId());

        String username = event.companyName();
        // If a user with that exact username exists, disambiguate with company code
        if (userRepository.existsByUsername(username)) {
            username = event.companyCode().toLowerCase() + "_admin";
            if (userRepository.existsByUsername(username)) {
                username = event.companyCode().toLowerCase() + "_" + event.companyId();
            }
        }

        String email = event.adminEmail();
        if (email == null || email.isBlank()) {
            email = event.companyCode().toLowerCase() + "_admin@gurukul.com";
        }
        if (userRepository.existsByEmail(email)) {
            email = event.companyCode().toLowerCase() + "_" + event.companyId() + "@gurukul.com";
        }

        String rawPassword = (event.adminPassword() != null && !event.adminPassword().isBlank())
                ? event.adminPassword()
                : "admin123";

        User adminUser = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .companyId(event.companyId())
                .access(Access.ADMIN)
                .enabled(true)
                .build();

        User saved = userRepository.save(adminUser);
        log.info("Provisioned admin user '{}' (id: {}) with role ADMIN for company '{}' (id: {})",
                saved.getUsername(), saved.getUserId(), event.companyName(), event.companyId());
    }
}
