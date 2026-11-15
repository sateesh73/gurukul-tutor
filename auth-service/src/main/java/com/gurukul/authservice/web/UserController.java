package com.gurukul.authservice.web;

import com.gurukul.authservice.dto.CreateUserRequest;
import com.gurukul.authservice.dto.UserDto;
import com.gurukul.authservice.security.TenantSecurityService;
import com.gurukul.authservice.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final TenantSecurityService tenantSecurity;

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        if (!tenantSecurity.canCreateUserInCompany(request.companyId())) {
            throw new AccessDeniedException("Access denied: You cannot create users for company ID " + request.companyId());
        }
        UserDto created = userService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.userId()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
        if (tenantSecurity.isGurukulAdmin()) {
            return ResponseEntity.ok(userService.findAll());
        }
        Long companyId = tenantSecurity.getCurrentCompanyId();
        if (companyId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(userService.findByCompanyId(companyId));
    }

    @GetMapping("/page")
    public ResponseEntity<Page<UserDto>> findAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        if (tenantSecurity.isGurukulAdmin()) {
            return ResponseEntity.ok(userService.findAll(pageable));
        }
        Long companyId = tenantSecurity.getCurrentCompanyId();
        if (companyId == null) {
            return ResponseEntity.ok(Page.empty(pageable));
        }
        return ResponseEntity.ok(userService.findByCompanyId(companyId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        if (!tenantSecurity.canAccessUser(id)) {
            throw new AccessDeniedException("Access denied: You cannot view user with ID " + id);
        }
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> findByUsername(@PathVariable String username) {
        if (!tenantSecurity.canAccessUsername(username)) {
            throw new AccessDeniedException("Access denied: You cannot view user with username " + username);
        }
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<UserDto>> findByCompanyId(@PathVariable Long companyId) {
        if (!tenantSecurity.canAccessCompany(companyId)) {
            throw new AccessDeniedException("Access denied: You cannot view users of company ID " + companyId);
        }
        return ResponseEntity.ok(userService.findByCompanyId(companyId));
    }

    @GetMapping("/company/{companyId}/page")
    public ResponseEntity<Page<UserDto>> findByCompanyIdPaged(
            @PathVariable Long companyId,
            @PageableDefault(size = 20) Pageable pageable) {
        if (!tenantSecurity.canAccessCompany(companyId)) {
            throw new AccessDeniedException("Access denied: You cannot view users of company ID " + companyId);
        }
        return ResponseEntity.ok(userService.findByCompanyId(companyId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        if (!tenantSecurity.canAccessUser(id)) {
            throw new AccessDeniedException("Access denied: You cannot modify user with ID " + id);
        }
        if (request.companyId() != null && !tenantSecurity.isGurukulAdmin()) {
            UserDto existing = userService.findById(id);
            if (!request.companyId().equals(existing.companyId())) {
                throw new AccessDeniedException("Access denied: You cannot move a user to another company.");
            }
        }
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!tenantSecurity.canAccessUser(id)) {
            throw new AccessDeniedException("Access denied: You cannot delete user with ID " + id);
        }
        userService.delete(id);
    }
}