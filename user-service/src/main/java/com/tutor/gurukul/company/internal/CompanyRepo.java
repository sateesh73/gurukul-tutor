package com.tutor.gurukul.company.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CompanyRepo extends JpaRepository<Company, String> {
}
