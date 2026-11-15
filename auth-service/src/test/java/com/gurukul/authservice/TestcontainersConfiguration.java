package com.gurukul.authservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Testcontainers configuration for integration testing.
 * Provisions both PostgreSQL and Keycloak containers to mirror production infrastructure.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"))
                .withDatabaseName("authdb")
                .withUsername("authuser")
                .withPassword("authpass")
                .withReuse(false);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public GenericContainer<?> keycloakContainer() {
        return new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:26.8.2"))
                .withExposedPorts(8080)
                .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
                .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
                .withEnv("KC_HTTP_ENABLED", "true")
                .withEnv("KC_HOSTNAME_STRICT", "false")
                .withCommand("start-dev")
                .waitingFor(Wait.forHttp("/realms/master/.well-known/openid-configuration")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(2)));
    }

    @Bean
    public DynamicPropertyRegistrar keycloakDynamicProperties(GenericContainer<?> keycloakContainer) {
        return registry -> {
            String authServerUrl = "http://" + keycloakContainer.getHost() + ":" + keycloakContainer.getMappedPort(8080);
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                    () -> authServerUrl + "/realms/master");
            registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                    () -> authServerUrl + "/realms/master/protocol/openid-connect/certs");
            registry.add("KEYCLOAK_ISSUER_URI", () -> authServerUrl + "/realms/master");
            registry.add("KEYCLOAK_HOST", keycloakContainer::getHost);
            registry.add("KEYCLOAK_PORT", () -> keycloakContainer.getMappedPort(8080));
            registry.add("KEYCLOAK_REALM", () -> "master");
        };
    }
}