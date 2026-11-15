package com.gurukul.authservice.user;

import com.gurukul.authservice.company.CompanyService;
import com.gurukul.authservice.dto.CreateUserRequest;
import com.gurukul.authservice.dto.UserDto;
import com.gurukul.authservice.exception.DuplicateResourceException;
import com.gurukul.authservice.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final PasswordEncoder passwordEncoder;

    // Default company code for Gurukul (ID: 1)
    private static final Long GURUKUL_COMPANY_ID = 1L;

    @Override
    public UserDto create(CreateUserRequest request) {
        // Company is mandatory - throw exception if not provided
        if (request.companyId() == null) {
            throw new ResourceNotFoundException("Company selection is mandatory. Please provide a valid company ID.");
        }

        // Company must exist (no auto-creation of TEMP company anymore)
        if (!companyService.existsById(request.companyId())) {
            throw new ResourceNotFoundException("Company not found with id: " + request.companyId() +
                ". Company must exist before creating users.");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        Access access = Access.fromString(request.role());
        String encodedPassword = encodePasswordIfNeeded(request.password());

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(encodedPassword)
                .companyId(request.companyId())
                .access(access)
                .enabled(true)
                .build();

        return toDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    public UserDto findById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return toDto(user);
    }

    @Override
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return toDto(user);
    }

    @Override
    public List<UserDto> findByCompanyId(Long companyId) {
        return userRepository.findByCompanyId(companyId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<UserDto> findByCompanyId(Long companyId, Pageable pageable) {
        return userRepository.findByCompanyId(companyId, pageable)
                .map(this::toDto);
    }

    @Override
    public UserDto update(Long userId, CreateUserRequest request) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Company must exist if provided and different from current
        if (request.companyId() != null && !request.companyId().equals(existing.getCompanyId())) {
            if (!companyService.existsById(request.companyId())) {
                throw new ResourceNotFoundException("Company not found with id: " + request.companyId());
            }
            existing.setCompanyId(request.companyId());
        }

        // Check duplicate username if changed
        if (request.username() != null && !request.username().equals(existing.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new DuplicateResourceException("Username already exists: " + request.username());
            }
            existing.setUsername(request.username());
        }

        // Check duplicate email if changed
        if (request.email() != null && !request.email().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("Email already exists: " + request.email());
            }
            existing.setEmail(request.email());
        }

        // Update password if provided
        if (request.password() != null && !request.password().isBlank()) {
            existing.setPassword(encodePasswordIfNeeded(request.password()));
        }

        // Update role/access if provided
        if (request.role() != null && !request.role().isBlank()) {
            existing.setAccess(Access.fromString(request.role()));
        }

        return toDto(userRepository.save(existing));
    }

    @Override
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Long createAdminUser(String username, String email, String password, Long companyId) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(encodePasswordIfNeeded(password))
                .companyId(companyId)
                .access(Access.ADMIN)
                .enabled(true)
                .build();
        User saved = userRepository.save(user);
        return saved.getUserId();
    }

    /**
     * Check if a user can create another user in the specified company.
     * Only ADMIN or ASSOCIATE can create users under their own company.
     * Gurukul ADMIN has access to create users in any company.
     */
    public boolean canCreateUserInCompany(Long creatorUserId, Long targetCompanyId) {
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found"));

        // Gurukul admin (companyId = 1) can create users in any company
        if (GURUKUL_COMPANY_ID.equals(creator.getCompanyId()) &&
            creator.getAccess() == Access.ADMIN) {
            return true;
        }

        // ADMIN or ASSOCIATE can only create users in their own company
        return (creator.getAccess() == Access.ADMIN || creator.getAccess() == Access.ASSOCIATE) &&
               creator.getCompanyId().equals(targetCompanyId);
    }

    private String encodePasswordIfNeeded(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        // If it's already a bcrypt hash, avoid re-encoding
        if (raw.startsWith("$2a$") || raw.startsWith("$2b$") || raw.startsWith("$2y$")) {
            return raw;
        }
        return passwordEncoder.encode(raw);
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getCompanyId(),
                user.getAccess(),
                user.getEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}