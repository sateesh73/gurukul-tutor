package com.gurukul.authservice.config;

import com.gurukul.authservice.company.CompanyService;
import com.gurukul.authservice.dto.CreateCompanyRequest;
import com.gurukul.authservice.user.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthDataInitializer {

    private final CompanyService companyService;
    private final UserService userService;

    @PostConstruct
    public void init() {
        // Create default Gurukul company if not exists
        if (!companyService.existsByCode("GURUKUL")) {
            companyService.create(new CreateCompanyRequest("Gurukul", "GURUKUL"));
        }

        // Create default admin user "sateesh" under Gurukul (ID: 1)
        // Gurukul admin has access to all company data
        if (!userService.existsByUsername("sateesh")) {
            userService.createAdminUser(
                    "sateesh",
                    "sateesh@gurukul.com",
                    "admin123", // TODO: hash this in production!
                    1L // Gurukul company ID
            );
        }
    }
}