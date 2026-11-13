package com.gurukul.authservice.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface UserRepository extends JpaRepository<com.gurukul.authservice.user.User, Long> {
    Optional<com.gurukul.authservice.user.User> findByUsername(String username);
    Optional<com.gurukul.authservice.user.User> findByEmail(String email);
    List<com.gurukul.authservice.user.User> findByCompanyId(Long companyId);
    Page<com.gurukul.authservice.user.User> findByCompanyId(Long companyId, Pageable pageable);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}