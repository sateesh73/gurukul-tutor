package com.gurukul.authservice.dto;

import java.time.LocalDateTime;

public record CompanyDto(
    Long companyId,
    String name,
    String code,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}