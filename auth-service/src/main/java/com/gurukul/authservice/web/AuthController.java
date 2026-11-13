package com.gurukul.authservice.web;

import com.gurukul.authservice.dto.UserDto;
import com.gurukul.authservice.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(userService.findById(userId));
    }
}