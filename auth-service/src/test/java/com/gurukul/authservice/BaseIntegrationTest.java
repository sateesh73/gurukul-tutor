package com.gurukul.authservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base test configuration for integration and repository tests.
 * Spins up a dedicated PostgreSQL Testcontainer with dynamic credentials and schema.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
public abstract class BaseIntegrationTest {
}
