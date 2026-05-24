package com.tutor.gurukul.common;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record Error(
        HttpStatus status,
        String message
) {
}
