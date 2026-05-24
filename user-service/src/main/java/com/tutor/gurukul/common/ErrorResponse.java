package com.tutor.gurukul.common;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ErrorResponse(
        List<Error> errorList,
        Instant timestamp,
        String path,
        String method,
        String traceId
) {
}
