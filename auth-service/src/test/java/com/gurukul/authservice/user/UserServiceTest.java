package com.gurukul.authservice.user;

import com.gurukul.authservice.BaseIntegrationTest;
import com.gurukul.authservice.company.CompanyService;
import com.gurukul.authservice.dto.CompanyDto;
import com.gurukul.authservice.dto.CreateCompanyRequest;
import com.gurukul.authservice.dto.CreateUserRequest;
import com.gurukul.authservice.dto.UserDto;
import com.gurukul.authservice.exception.DuplicateResourceException;
import com.gurukul.authservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User Service Tests with Testcontainers")
class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should create a new user with actual member profile data")
    void createUser() {
        // Given - Real academic student profile
        CreateUserRequest request = new CreateUserRequest(
                "rajesh.koothrappali", "rajesh.k@gurukul.com", "RajeshPass@2026", 1L, "STUDENT");

        // When
        UserDto result = userService.create(request);

        // Then
        assertThat(result.userId()).isNotNull();
        assertThat(result.username()).isEqualTo("rajesh.koothrappali");
        assertThat(result.email()).isEqualTo("rajesh.k@gurukul.com");
        assertThat(result.companyId()).isEqualTo(1L);
        assertThat(result.access()).isEqualTo(Access.STUDENT);
        assertThat(result.enabled()).isTrue();
    }

    @Test
    @DisplayName("Should securely hash user password using BCrypt upon creation")
    void shouldHashPasswordOnUserCreation() {
        CreateUserRequest request = new CreateUserRequest(
                "vikram.sarabhai", "vikram.s@isro.gov.in", "SarabhaiSecure#2026", 1L, "TEACHER");

        UserDto created = userService.create(request);

        User saved = userRepository.findById(created.userId()).orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("SarabhaiSecure#2026");
        assertThat(saved.getPassword()).startsWith("$2a$");
        assertThat(passwordEncoder.matches("SarabhaiSecure#2026", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Should default to TEMP access level when no specific role is designated")
    void createUserWithDefaultAccess() {
        // Given - New registration without access specified
        CreateUserRequest request = new CreateUserRequest(
                "kavita.krishnan", "kavita.k@gurukul.com", "KavitaPass@2026", 1L, null);

        // When
        UserDto result = userService.create(request);

        // Then
        assertThat(result.access()).isEqualTo(Access.TEMP);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when registering existing username")
    void createUserDuplicateUsername() {
        // Given - Existing faculty member
        userRepository.save(User.builder()
                .username("sanjay.sharma")
                .email("sanjay.sharma@tcs.com")
                .password("SanjayPass@2026")
                .companyId(1L)
                .access(Access.TEACHER)
                .build());

        CreateUserRequest duplicateRequest = new CreateUserRequest(
                "sanjay.sharma", "different.sanjay@tcs.com", "Password@123", 1L, "TEACHER");

        // When / Then
        assertThatThrownBy(() -> userService.create(duplicateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists: sanjay.sharma");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when registering existing email")
    void createUserDuplicateEmail() {
        // Given - Existing associate member
        userRepository.save(User.builder()
                .username("deepa.menon")
                .email("deepa.menon@infosys.com")
                .password("DeepaPass@2026")
                .companyId(1L)
                .access(Access.ASSOCIATE)
                .build());

        CreateUserRequest duplicateEmailRequest = new CreateUserRequest(
                "deepa.second.account", "deepa.menon@infosys.com", "Password@123", 1L, "ASSOCIATE");

        // When / Then
        assertThatThrownBy(() -> userService.create(duplicateEmailRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists: deepa.menon@infosys.com");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when company ID is omitted")
    void createUserWithoutCompany() {
        // Given - Request missing mandatory company affiliation
        CreateUserRequest request = new CreateUserRequest(
                "unaffiliated.user", "unaffiliated@gurukul.com", "SecurePass@2026", null, "TEMP");

        // When / Then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company selection is mandatory");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when assigned company does not exist")
    void createUserWithNonExistentCompany() {
        // Given - Request targeting non-existent company ID
        CreateUserRequest request = new CreateUserRequest(
                "orphan.user", "orphan@gurukul.com", "SecurePass@2026", 999999L, "TEMP");

        // When / Then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found with id: 999999");
    }

    @Test
    @DisplayName("Should retrieve member profile by user ID")
    void findUserById() {
        // Given - Seeded user
        User saved = userRepository.save(User.builder()
                .username("sunil.gavaskar")
                .email("sunil.g@gurukul.com")
                .password("SunilPass@2026")
                .companyId(1L)
                .access(Access.TEACHER)
                .build());

        // When
        UserDto result = userService.findById(saved.getUserId());

        // Then
        assertThat(result.username()).isEqualTo("sunil.gavaskar");
        assertThat(result.email()).isEqualTo("sunil.g@gurukul.com");
        assertThat(result.access()).isEqualTo(Access.TEACHER);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when querying non-existent user ID")
    void findUserByIdNotFound() {
        assertThatThrownBy(() -> userService.findById(999999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999999");
    }

    @Test
    @DisplayName("Should find registered user by username")
    void findByUsername() {
        // Given
        userRepository.save(User.builder()
                .username("amitabh.bachchan")
                .email("amitabh.b@gurukul.com")
                .password("AmitabhPass@2026")
                .companyId(1L)
                .access(Access.ADMIN)
                .build());

        // When
        UserDto result = userService.findByUsername("amitabh.bachchan");

        // Then
        assertThat(result.username()).isEqualTo("amitabh.bachchan");
        assertThat(result.email()).isEqualTo("amitabh.b@gurukul.com");
        assertThat(result.access()).isEqualTo(Access.ADMIN);
    }

    @Test
    @DisplayName("Should find all students and teachers enrolled in a specific university partner")
    void findByCompanyId() {
        // Given - Real partner university created through service
        CompanyDto university = companyService.create(
                new CreateCompanyRequest("Birla Institute of Technology", "BITS_PILANI"));

        Long universityId = university.companyId();

        userRepository.saveAll(List.of(
                User.builder().username("arun.kumar").email("arun.kumar@bits.edu").password("BitsPass1@2026").companyId(universityId).access(Access.TEACHER).build(),
                User.builder().username("divya.ramesh").email("divya.ramesh@bits.edu").password("BitsPass2@2026").companyId(universityId).access(Access.STUDENT).build()
        ));

        // When
        List<UserDto> enrolledUsers = userService.findByCompanyId(universityId);

        // Then - Includes the auto-provisioned company admin plus the 2 added members
        assertThat(enrolledUsers).hasSize(3);
        assertThat(enrolledUsers).allMatch(u -> u.companyId().equals(universityId));
        assertThat(enrolledUsers)
                .extracting(UserDto::username)
                .containsExactlyInAnyOrder("Birla Institute of Technology", "arun.kumar", "divya.ramesh");
    }

    @Test
    @DisplayName("Should update user profile information")
    void updateUser() {
        // Given - Initial user profile
        User initial = userRepository.save(User.builder()
                .username("initial.faculty")
                .email("initial.faculty@gurukul.com")
                .password("InitialPass@2026")
                .companyId(1L)
                .access(Access.TEMP)
                .build());

        CreateUserRequest updateRequest = new CreateUserRequest(
                "promoted.faculty", "promoted.faculty@gurukul.com", "PromotedPass@2026", 1L, "TEACHER");

        // When
        UserDto updated = userService.update(initial.getUserId(), updateRequest);

        // Then
        assertThat(updated.username()).isEqualTo("promoted.faculty");
        assertThat(updated.email()).isEqualTo("promoted.faculty@gurukul.com");
        assertThat(updated.access()).isEqualTo(Access.TEACHER);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating to an existing username")
    void updateDuplicateUsername() {
        userRepository.save(User.builder().username("existing.account1").email("account1@gurukul.com").password("pass").companyId(1L).build());
        User target = userRepository.save(User.builder().username("target.account").email("target@gurukul.com").password("pass").companyId(1L).build());

        CreateUserRequest conflict = new CreateUserRequest("existing.account1", "target@gurukul.com", "pass", 1L, "STUDENT");

        assertThatThrownBy(() -> userService.update(target.getUserId(), conflict))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating to an existing email")
    void updateDuplicateEmail() {
        userRepository.save(User.builder().username("existing.account2").email("account2@gurukul.com").password("pass").companyId(1L).build());
        User target = userRepository.save(User.builder().username("target.account2").email("target2@gurukul.com").password("pass").companyId(1L).build());

        CreateUserRequest conflict = new CreateUserRequest("target.account2", "account2@gurukul.com", "pass", 1L, "STUDENT");

        assertThatThrownBy(() -> userService.update(target.getUserId(), conflict))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Should delete member record from system")
    void deleteUser() {
        // Given - Graduated student
        User student = userRepository.save(User.builder()
                .username("graduated.student")
                .email("graduated@gurukul.com")
                .password("GradPass@2026")
                .companyId(1L)
                .access(Access.STUDENT)
                .build());

        // When
        userService.delete(student.getUserId());

        // Then
        assertThatThrownBy(() -> userService.findById(student.getUserId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should retrieve full list of users across all companies")
    void findAllUsers() {
        // Given - Add faculty members
        userRepository.saveAll(List.of(
                User.builder().username("faculty.alpha").email("faculty.alpha@gurukul.com").password("AlphaPass@2026").companyId(1L).access(Access.TEACHER).build(),
                User.builder().username("faculty.beta").email("faculty.beta@gurukul.com").password("BetaPass@2026").companyId(1L).access(Access.TEACHER).build()
        ));

        // When
        List<UserDto> allUsers = userService.findAll();

        // Then - Should contain seeded admin plus created faculty
        assertThat(allUsers).hasSizeGreaterThanOrEqualTo(3);
        assertThat(allUsers)
                .extracting(UserDto::username)
                .contains("faculty.alpha", "faculty.beta");
    }

    @Test
    @DisplayName("Should paginate user records with findAll(Pageable)")
    void findAllPaged() {
        Page<UserDto> page = userService.findAll(PageRequest.of(0, 2));

        assertThat(page).isNotNull();
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getContent()).isNotEmpty();
    }
}