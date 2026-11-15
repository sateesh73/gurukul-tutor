package com.gurukul.authservice.user;

import com.gurukul.authservice.dto.CreateUserRequest;
import com.gurukul.authservice.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserDto create(CreateUserRequest request);

    List<UserDto> findAll();

    Page<UserDto> findAll(Pageable pageable);

    UserDto findById(Long userId);

    UserDto findByUsername(String username);

    List<UserDto> findByCompanyId(Long companyId);

    Page<UserDto> findByCompanyId(Long companyId, Pageable pageable);

    UserDto update(Long userId, CreateUserRequest request);

    void delete(Long userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Long createAdminUser(String username, String email, String password, Long companyId);
}