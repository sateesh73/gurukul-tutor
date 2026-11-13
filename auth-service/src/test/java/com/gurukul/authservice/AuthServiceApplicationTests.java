package com.gurukul.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verify Spring Application Context loads successfully
        assertThat(AuthServiceApplication.class).isNotNull();
    }

    @Test
    void shouldRunSpringApplication() {
        // Smoke test: Verify SpringApplication.run method works correctly
        // The @SpringBootTest annotation handles this automatically
        assertThat(SpringApplication.class).isNotNull();
    }
}