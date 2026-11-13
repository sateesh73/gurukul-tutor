package com.gurukul.authservice.dto;

public record CreateUserRequest(
    String username,
    String email,
    String password,
    Long companyId,
    String role
) {}