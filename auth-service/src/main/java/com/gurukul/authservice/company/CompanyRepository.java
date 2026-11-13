package com.gurukul.authservice.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}