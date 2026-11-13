package com.gurukul.authservice.user;

import com.gurukul.authservice.dto.CreateUserRequest;
import com.gurukul.authservice.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(CreateUserRequest request);
    List<UserDto> findAll();
    UserDto findById(Long userId);
    UserDto findByUsername(String username);
    List<UserDto> findByCompanyId(Long companyId);
    UserDto update(Long userId, CreateUserRequest request);
    void delete(Long userId);
    boolean existsByUsername(String username);
    Long createAdminUser(String username, String email, String password, Long companyId);
}