package com.tutor.gurukul.company.model;

import lombok.Builder;

@Builder
public record CompanyRequest(
        String name,
        String email,
        String description,
        String websiteUrl,
        String logoUrl
) {
}
