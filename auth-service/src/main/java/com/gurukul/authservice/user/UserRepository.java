package com.gurukul.authservice.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Package-private repository for User entity operations.
 * Enforces feature boundary encapsulation within com.gurukul.authservice.user.
 */
interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByCompanyId(Long companyId);

    Page<User> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}