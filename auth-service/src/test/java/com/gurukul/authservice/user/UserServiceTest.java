package com.gurukul.authservice.user;

import com.gurukul.authservice.dto.CreateCompanyRequest;
import com.gurukul.authservice.dto.CreateUserRequest;
import com.gurukul.authservice.dto.UserDto;
import com.gurukul.authservice.exception.DuplicateResourceException;
import com.gurukul.authservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("User Service Tests")
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    com.gurukul.authservice.company.CompanyService companyService;

    @Test
    @DisplayName("Should create a new user")
    void createUser() {
        // Given - use a guaranteed existing company (Gurukul created by AuthDataInitializer)
        CreateUserRequest request = new CreateUserRequest("john_doe_unique_test1", "john_unique_test1@example.com", "secret123", 1L, null);

        // When
        UserDto result = userService.create(request);

        // Then
        assertThat(result.userId()).isNotNull();
        assertThat(result.username()).isEqualTo("john_doe_unique_test1");
        assertThat(result.email()).isEqualTo("john_unique_test1@example.com");
        assertThat(result.companyId()).isEqualTo(1L);
        assertThat(result.access()).isEqualTo(Access.TEMP);
        assertThat(result.enabled()).isTrue();
    }

    @Test
    @DisplayName("Should create user with default access TEMP")
    void createUserWithDefaultAccess() {
        // Given - Gurukul company (ID 1) is created by AuthDataInitializer
        CreateUserRequest request = new CreateUserRequest(
                "jane_doe_unique_test2", "jane_unique_test2@example.com", "password123", 1L, null);

        // When
        UserDto result = userService.create(request);

        // Then
        assertThat(result.access()).isEqualTo(Access.TEMP);
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void createUserDuplicateUsername() {
        // Given - create a user with unique username/email first
        User existing = userRepository.save(User.builder()
                .username("existing_unique_user_test3")
                .email("existing_unique3@example.com")
                .password("pass")
                .companyId(1L)
                .build());

        CreateUserRequest request = new CreateUserRequest(
                "existing_unique_user_test3", "different_email_test3_unique456@example.com", "password", 1L, null);

        // When / Then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void createUserDuplicateEmail() {
        // Given - create a user with unique username but specific email
        User existing = userRepository.save(User.builder()
                .username("unique_username_test4")
                .email("duplicate_email_test4_unique789@example.com")
                .password("pass")
                .companyId(1L)
                .build());

        CreateUserRequest request = new CreateUserRequest(
                "different_username_test4_unique789", "duplicate_email_test4_unique789@example.com", "password", 1L, null);

        // When / Then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Should throw exception when company is null")
    void createUserWithoutCompany() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "testuser_no_company", "test_no_company_unique101@example.com", "password", null, null);

        // When / Then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company selection is mandatory");
    }

    @Test
    @DisplayName("Should throw exception when company does not exist")
    void createUserWithNonExistentCompany() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "testuser_nonexistent", "test_nonexistent_unique102@example.com", "password", 999L, null);

        // When / Then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");
    }

    @Test
    @DisplayName("Should find user by id")
    void findUserById() {
        // Given - Gurukul company (ID 1) is created by AuthDataInitializer
        User saved = userRepository.save(User.builder()
                .username("findme_unique_test5")
                .email("findme_unique5@example.com")
                .password("pass")
                .companyId(1L)
                .build());

        // When
        UserDto result = userService.findById(saved.getUserId());

        // Then
        assertThat(result.username()).isEqualTo("findme_unique_test5");
        assertThat(result.companyId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void findUserByIdNotFound() {
        assertThatThrownBy(() -> userService.findById(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should find user by username")
    void findByUsername() {
        // Given - Gurukul company (ID 1) is created by AuthDataInitializer
        userRepository.save(User.builder()
                .username("unique_user_test6")
                .email("unique_test6_unique456@example.com")
                .password("pass")
                .companyId(1L)
                .build());

        // When
        UserDto result = userService.findByUsername("unique_user_test6");

        // Then
        assertThat(result.username()).isEqualTo("unique_user_test6");
    }

    @Test
    @DisplayName("Should find all users by company id")
    void findByCompanyId() {
        // Given - create a company through the service with unique code
        com.gurukul.authservice.dto.CompanyDto companyDto = companyService.create(
                new CreateCompanyRequest("TestCompanyFindByCompanyIdUnique999", "TESTCOMP999FINDID"));

        Long companyId = companyDto.companyId();

        userRepository.saveAll(List.of(
                User.builder().username("emp1_unique_test7").email("emp1_unique7@example.com").password("pass").companyId(companyId).build(),
                User.builder().username("emp2_unique_test8").email("emp2_unique8@example.com").password("pass").companyId(companyId).build()
        ));

        // When
        List<UserDto> result = userService.findByCompanyId(companyId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(u -> u.companyId().equals(companyId));
    }

    @Test
    @DisplayName("Should update user")
    void updateUser() {
        // Given - Gurukul company (ID 1) is created by AuthDataInitializer
        User saved = userRepository.save(User.builder()
                .username("olduser_unique_test9")
                .email("old_unique9@example.com")
                .password("pass")
                .companyId(1L)
                .access(Access.TEMP)
                .build());

        CreateUserRequest request = new CreateUserRequest("newuser_unique_test9", "new_unique9_unique456@example.com", "newpass", 1L, null);

        // When
        UserDto result = userService.update(saved.getUserId(), request);

        // Then
        assertThat(result.username()).isEqualTo("newuser_unique_test9");
        assertThat(result.companyId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should delete user")
    void deleteUser() {
        // Given - Gurukul company (ID 1) is created by AuthDataInitializer
        User saved = userRepository.save(User.builder()
                .username("delete_me_unique_test10")
                .email("delete_unique10@example.com")
                .password("pass")
                .companyId(1L)
                .access(Access.TEMP)
                .build());

        // When
        userService.delete(saved.getUserId());

        // Then
        assertThatThrownBy(() -> userService.findById(saved.getUserId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find all users")
    void findAllUsers() {
        // Given - create unique test users
        userRepository.saveAll(List.of(
                User.builder().username("list1_unique_test11").email("list1_unique11@example.com").password("p").companyId(1L).build(),
                User.builder().username("list2_unique_test12").email("list2_unique12@example.com").password("p").companyId(1L).build()
        ));

        // When
        List<UserDto> result = userService.findAll();

        // Then - should have at least 2 users (our test users, plus possibly sateesh from AuthDataInitializer)
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
    }
}