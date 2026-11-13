package com.gurukul.authservice.dto;

public record CreateCompanyRequest(
    String name,
    String code
) {}