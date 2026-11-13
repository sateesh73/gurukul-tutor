package com.gurukul.authservice.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("Should save and find user by id")
    void saveAndFindById() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("pass")
                .companyId(1L)
                .build();

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");

        User found = userRepository.findById(saved.getUserId()).orElseThrow();
        assertThat(found.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should find user by username")
    void findByUsername() {
        // Given
        userRepository.save(User.builder()
                .username("findme")
                .email("findme@example.com")
                .password("pass")
                .companyId(1L)
                .build());

        // When
        User found = userRepository.findByUsername("findme").orElseThrow();

        // Then
        assertThat(found.getUsername()).isEqualTo("findme");
    }

    @Test
    @DisplayName("Should find user by email")
    void findByEmail() {
        // Given
        userRepository.save(User.builder()
                .username("emailuser")
                .email("email@example.com")
                .password("pass")
                .companyId(1L)
                .build());

        // When
        User found = userRepository.findByEmail("email@example.com").orElseThrow();

        // Then
        assertThat(found.getEmail()).isEqualTo("email@example.com");
    }

    @Test
    @DisplayName("Should find users by company id")
    void findByCompanyId() {
        // Given
        userRepository.saveAll(List.of(
                User.builder().username("emp1").email("emp1@example.com").password("pass").companyId(10L).build(),
                User.builder().username("emp2").email("emp2@example.com").password("pass").companyId(10L).build(),
                User.builder().username("emp3").email("emp3@example.com").password("pass").companyId(20L).build()
        ));

        // When
        List<User> users = userRepository.findByCompanyId(10L);

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).allMatch(u -> u.getCompanyId().equals(10L));
    }

    @Test
    @DisplayName("Should check if username exists")
    void existsByUsername() {
        // Given
        userRepository.save(User.builder()
                .username("existing")
                .email("existing@example.com")
                .password("pass")
                .companyId(1L)
                .build());

        // When & Then
        assertThat(userRepository.existsByUsername("existing")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Should check if email exists")
    void existsByEmail() {
        // Given
        userRepository.save(User.builder()
                .username("user2")
                .email("unique@example.com")
                .password("pass")
                .companyId(1L)
                .build());

        // When & Then
        assertThat(userRepository.existsByEmail("unique@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
    }
}