package com.tutor.gurukul.company.model;

import lombok.Builder;

@Builder
public record CompanyResponse(
        String id,
        String name,
        String email,
        String description,
        String websiteUrl,
        String logoUrl
) {
}
