package com.tutor.gurukul.users.web;

import com.tutor.gurukul.common.ErrorResponse;
import com.tutor.gurukul.common.Error;
import com.tutor.gurukul.users.exception.UserAlreadyExist;
import com.tutor.gurukul.users.exception.UserNotFoundException;
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
public class UsersExceptionAdvise {

    private final String traceId = RandomGenerator.getDefault().nextLong(1_000_000_000L, 9_999_999_999L) + "";
    private final String path = "/api/v1/users";

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getName());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorList(List.of(Error.builder()
                        .status(HttpStatus.NOT_FOUND)
                        .message(ex.getName())
                        .build()))
                .timestamp(Instant.now())
                .path(path+"/{id}")
                .method(HttpMethod.GET.name())
                .traceId(traceId)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistException(UserAlreadyExist ex) {
        log.error("User already exists: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorList(List.of(Error.builder()
                        .status(HttpStatus.CONFLICT)
                        .message(ex.getMessage())
                        .build()))
                .timestamp(Instant.now())
                .path(path)
                .method(HttpMethod.POST.name())
                .traceId(traceId)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorList(List.of(Error.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message(ex.getMessage())
                        .build()))
                .timestamp(Instant.now())
                .path(path)
                .method(HttpMethod.POST.name())
                .traceId(traceId)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}