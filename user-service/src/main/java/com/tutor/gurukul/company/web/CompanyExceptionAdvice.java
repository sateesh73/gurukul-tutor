package com.tutor.gurukul.company.web;

import com.tutor.gurukul.common.ErrorResponse;
import com.tutor.gurukul.common.Error;
import com.tutor.gurukul.company.exception.CompanyAlreadyExistsException;
import com.tutor.gurukul.company.exception.CompanyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.random.RandomGenerator;

@RestControllerAdvice
@Slf4j
public class CompanyExceptionAdvice {

    private final String traceId = RandomGenerator.getDefault().nextLong(1_000_000_000L, 9_999_999_999L) + "";
    private final String basePath = "/api/v1/company";


    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCompanyNotFoundException(CompanyNotFoundException ex) {
        log.error("Company not found: {}", ex.getName());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .errorList(List.of(Error.builder()
                            .status(HttpStatus.NOT_FOUND)
                            .message(ex.getName())
                            .build()))
                    .timestamp(Instant.now())
                    .path(basePath + "/{id}")
                    .method(HttpMethod.GET.name())
                    .traceId(traceId)
                    .build();
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(CompanyAlreadyExistsException.class)
       public ResponseEntity<ErrorResponse> handleCompanyAlreadyExistsException(CompanyAlreadyExistsException ex) {
        log.error("Company already exists: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorList(List.of(Error.builder()
                        .status(HttpStatus.CONFLICT)
                        .message(ex.getMessage())
                        .build()))
                .timestamp(Instant.now())
                .path(basePath)
                .method(HttpMethod.POST.name())
                .traceId(traceId)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorList(List.of(Error.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message( ex.getMessage())
                        .build()))
                .timestamp(Instant.now())
                .path(basePath)
                .method(HttpMethod.POST.name())
                .traceId(traceId)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
