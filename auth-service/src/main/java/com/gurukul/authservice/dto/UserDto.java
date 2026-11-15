package com.gurukul.authservice.dto;

import com.gurukul.authservice.user.Access;
import java.time.LocalDateTime;

public record UserDto(
    Long userId,
    String username,
    String email,
    Long companyId,
    Access access,
    Boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}