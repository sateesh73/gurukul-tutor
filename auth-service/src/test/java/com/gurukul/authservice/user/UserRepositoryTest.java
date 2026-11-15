package com.gurukul.authservice.user;

import com.gurukul.authservice.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Repository Tests with Testcontainers")
class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and find academic user by id with actual profile data")
    void saveAndFindById() {
        // Given - Real teacher profile in Gurukul system
        User teacher = User.builder()
                .username("dr.ramesh.sharma")
                .email("ramesh.sharma@gurukul.com")
                .password("TeacherSecure@2026")
                .companyId(1L)
                .access(Access.TEACHER)
                .enabled(true)
                .build();

        // When
        User saved = userRepository.save(teacher);

        // Then
        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("dr.ramesh.sharma");
        assertThat(saved.getAccess()).isEqualTo(Access.TEACHER);

        User found = userRepository.findById(saved.getUserId()).orElseThrow();
        assertThat(found.getEmail()).isEqualTo("ramesh.sharma@gurukul.com");
        assertThat(found.getAccess()).isEqualTo(Access.TEACHER);
    }

    @Test
    @DisplayName("Should find registered user by username")
    void findByUsername() {
        // Given - Real partner enterprise associate
        userRepository.save(User.builder()
                .username("priya.nair")
                .email("priya.nair@tcs.com")
                .password("TcsAssociate#2026")
                .companyId(1L)
                .access(Access.ASSOCIATE)
                .enabled(true)
                .build());

        // When
        User found = userRepository.findByUsername("priya.nair").orElseThrow();

        // Then
        assertThat(found.getUsername()).isEqualTo("priya.nair");
        assertThat(found.getEmail()).isEqualTo("priya.nair@tcs.com");
        assertThat(found.getAccess()).isEqualTo(Access.ASSOCIATE);
    }

    @Test
    @DisplayName("Should find enrolled student by email address")
    void findByEmail() {
        // Given - Real student enrolled in tutoring program
        userRepository.save(User.builder()
                .username("ananya.patel")
                .email("ananya.patel@stanford.edu")
                .password("StudentPass!2026")
                .companyId(1L)
                .access(Access.STUDENT)
                .enabled(true)
                .build());

        // When
        User found = userRepository.findByEmail("ananya.patel@stanford.edu").orElseThrow();

        // Then
        assertThat(found.getUsername()).isEqualTo("ananya.patel");
        assertThat(found.getEmail()).isEqualTo("ananya.patel@stanford.edu");
        assertThat(found.getAccess()).isEqualTo(Access.STUDENT);
    }

    @Test
    @DisplayName("Should find all active members enrolled under a specific organization")
    void findByCompanyId() {
        // Given - Multiple employees belonging to organization ID 10
        Long enterpriseCompanyId = 10L;
        userRepository.saveAll(List.of(
                User.builder().username("rohit.verma").email("rohit.verma@wipro.com").password("WiproPass1@2026").companyId(enterpriseCompanyId).access(Access.STUDENT).build(),
                User.builder().username("sneha.reddy").email("sneha.reddy@wipro.com").password("WiproPass2@2026").companyId(enterpriseCompanyId).access(Access.ASSOCIATE).build(),
                User.builder().username("karan.mehta").email("karan.mehta@tcs.com").password("TcsPass3@2026").companyId(20L).access(Access.TEACHER).build()
        ));

        // When
        List<User> enterpriseUsers = userRepository.findByCompanyId(enterpriseCompanyId);

        // Then
        assertThat(enterpriseUsers).hasSize(2);
        assertThat(enterpriseUsers).allMatch(u -> u.getCompanyId().equals(enterpriseCompanyId));
        assertThat(enterpriseUsers)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("rohit.verma", "sneha.reddy");
    }

    @Test
    @DisplayName("Should accurately check if username is already registered")
    void existsByUsername() {
        // Given - Real registered member
        userRepository.save(User.builder()
                .username("arjun.mehta")
                .email("arjun.mehta@gurukul.com")
                .password("ArjunPass@2026")
                .companyId(1L)
                .access(Access.TEACHER)
                .build());

        // When & Then
        assertThat(userRepository.existsByUsername("arjun.mehta")).isTrue();
        assertThat(userRepository.existsByUsername("unregistered.faculty")).isFalse();
    }

    @Test
    @DisplayName("Should accurately check if email address is already registered")
    void existsByEmail() {
        // Given - Real registered member
        userRepository.save(User.builder()
                .username("neha.gupta")
                .email("neha.gupta@acme.com")
                .password("AcmePass#2026")
                .companyId(1L)
                .access(Access.TEMP)
                .build());

        // When & Then
        assertThat(userRepository.existsByEmail("neha.gupta@acme.com")).isTrue();
        assertThat(userRepository.existsByEmail("available.email@domain.com")).isFalse();
    }
}