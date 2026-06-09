package com.tutor.gurukul.users.internal;

import com.tutor.gurukul.users.UserService;
import com.tutor.gurukul.users.exception.UserAlreadyExist;
import com.tutor.gurukul.users.exception.UserNotFoundException;
import com.tutor.gurukul.users.model.UserRequest;
import com.tutor.gurukul.users.model.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    /**
     * Creates a new user based on the provided UserRequest.
     *
     * @param userRequest the request object containing user details; must not be null and should contain required fields (e.g. name, email).
     * @throws UserAlreadyExist if the userRequest is invalid or if a user with the same email already exists.
     */
    @Override
    public void createUser(UserRequest userRequest) throws UserAlreadyExist {
        log.info("Creating user with email {}", userRequest.email());
        var user = userTo(userRequest);
        var existingUser = userRepo.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExist("User with email " + user.getEmail() + " already exists");
        }
        userRepo.save(user);
        log.info("User with email {} created successfully", user.getEmail());
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve; must not be null.
     * @throws UserNotFoundException if the userId is null.
     */
    @Override
    public void updateUser(String userId, UserRequest userRequest) throws UserNotFoundException {
        log.info("Updating user with ID {}", userId);
        var existingUser = userRepo.findById(userId);
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }
        var user = existingUser.get();
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setEmail(userRequest.email());
        user.setPhoneNumber(userRequest.phoneNumber());
        user.setPassword(userRequest.password());
        user.setCompanyId(userRequest.companyId());
        user.setRole(Role.valueOf(userRequest.role()));
        user.setAddress(addressTo(userRequest));
        userRepo.save(user);
        log.info("User with ID {} updated successfully", userId);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param userId the ID of the user to delete; must not be null.
     * @throws UserNotFoundException if the userId is null.
     */
    @Override
    public void deleteUser(String userId) throws UserNotFoundException {
        log.info("Deleting user with ID {}", userId);
        var existingUser = userRepo.findById(userId);
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }
        userRepo.delete(existingUser.get());
        log.info("User with ID {} deleted successfully", userId);
    }

    /**
     * Retrieves all users.
     *
     * @return a list of UserResponse objects containing all users; will be empty if no users are found.
     */
    @Override
    public List<UserResponse> getAllUsers() {
        log.info("Retrieving all users");
        var users = userRepo.findAll();
        if (users.isEmpty()) {
            log.info("No users found");
            return List.of();
        }
        return users.stream()
                .map(this::userResponseTo)
                .toList();
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId the ID of the user to retrieve; must not be null.
     * @return the UserResponse object containing the user details; will be null if no user is found with the given ID.
     * @throws UserNotFoundException if the userId is null.
     */
    @Override
    public UserResponse getUserById(String userId) throws UserNotFoundException {
        log.info("Retrieving user with ID {}", userId);
        var existingUser = userRepo.findById(userId);
        if (existingUser.isEmpty()) {
            log.info("User with ID {} not found", userId);
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }
        return userResponseTo(existingUser.get());
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user to retrieve; must not be null and should be a valid email format.
     * @return the UserResponse object containing the user details; will be null if no user is found with the given email.
     * @throws UserNotFoundException if the email is null or not in a valid format.
     */
    @Override
    public UserResponse getUserByEmail(String email) throws UserNotFoundException {
        log.info("Retrieving user with email {}", email);
        var existingUser = userRepo.findByEmail(email);
        if (existingUser.isEmpty()) {
            log.info("User with email {} not found", email);
            throw new UserNotFoundException("User with email " + email + " not found");
        }
        return userResponseTo(existingUser.get());
    }

    /**
     * Retrieves a list of users associated with a specific company ID.
     *
     * @param companyId the ID of the company whose users are to be retrieved; must not be null.
     * @return a list of UserResponse objects containing the details of users associated with the specified company; will be empty if no users are found for the given company ID.
     * @throws UserNotFoundException if the companyId is null.
     */
    @Override
    public List<UserResponse> getUsersByCompanyId(String companyId) throws UserNotFoundException {
        log.info("Retrieving users for company with ID {}", companyId);
        var users = userRepo.findByCompanyId(companyId);
        if (users.isEmpty()) {
            log.info("No users found for company with ID {}", companyId);
            throw new UserNotFoundException("User with company ID " + companyId + " not found");
        }
        return users.get().stream()
                .map(this::userResponseTo)
                .toList();
    }

    /**
     * Deletes all users that belong to the given companyId.
     * <p>
     * This method validates the input and then safely resolves the repository result without
     * calling Optional.get() unguarded to avoid NoSuchElementException/NPE.
     * If no users are found for the company, a {@link UserNotFoundException} is thrown.
     *
     * @param companyId the id of the company whose users should be deleted; must not be null
     * @throws UserNotFoundException when companyId is null or no users exist for the company
     */
    @Transactional
    @Override
    public void deleteUsersByCompanyId(String companyId) throws UserNotFoundException {
        log.info("Deleting users for company with ID {}", companyId);

        if (companyId == null) {
            log.warn("companyId is null");
            throw new UserNotFoundException("companyId must not be null");
        }

        var users = userRepo.findByCompanyId(companyId)
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new UserNotFoundException("User with company ID " + companyId + " not found"));

        userRepo.deleteAll(users);
        log.info("Deleted {} users for company with ID {}", users.size(), companyId);
    }

    private Address addressTo(UserRequest userRequest) {
        return Address.builder()
                .street(userRequest.street())
                .city(userRequest.city())
                .state(userRequest.state())
                .zipCode(userRequest.zipCode())
                .country(userRequest.country())
                .build();
    }

    /**
     * Helper method to convert Users entity to UserResponse.
     * This method takes a Users entity and maps its fields to a UserResponse object, which can then be returned to the client or used for further processing within the service layer.
     *
     * @param user the Users entity containing the user details to be converted.
     * @return a UserResponse object populated with the user details from the Users entity.
     */
    private UserResponse userResponseTo(Users user) {
        var address = user.getAddress();
        return UserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .companyId(user.getCompanyId())
                .role(user.getRole().toString())
                .street(address.street())
                .city(address.city())
                .state(address.state())
                .zipCode(address.zipCode())
                .country(address.country())
                .build();
    }

    /**
     * Helper method to convert UserRequest to Users entity.
     * This method takes a UserRequest object and maps its fields to a Users entity, which can then be used for persistence or further processing within the service layer.
     *
     * @param userRequest the UserRequest object containing the user details to be converted.
     * @return a Users entity populated with the user details from the UserRequest.
     */
    private Users userTo(UserRequest userRequest) {
        return Users.builder()
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .email(userRequest.email())
                .phoneNumber(userRequest.phoneNumber())
                .password(userRequest.password())
                .companyId(userRequest.companyId())
                .role(Role.valueOf(userRequest.role()))
                .address(addressTo(userRequest))
                .build();
    }
}
