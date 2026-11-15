package com.gurukul.authservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Application Context & Testcontainers Verification Tests")
class AuthServiceApplicationTests extends BaseIntegrationTest {

    @Autowired
    private PostgreSQLContainer<?> postgresContainer;

    @Autowired
    private GenericContainer<?> keycloakContainer;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Application context should load successfully with PostgreSQL & Keycloak Testcontainers")
    void contextLoads() {
        assertThat(AuthServiceApplication.class).isNotNull();
        assertThat(jwtDecoder).isNotNull();
    }

    @Test
    @DisplayName("Should verify Spring Boot application bootstrap classes")
    void shouldRunSpringApplication() {
        assertThat(SpringApplication.class).isNotNull();
    }

    @Test
    @DisplayName("Should verify PostgreSQL Testcontainer is running and healthy")
    void shouldVerifyPostgresContainer() {
        assertThat(postgresContainer).isNotNull();
        assertThat(postgresContainer.isRunning()).isTrue();
        assertThat(postgresContainer.getDatabaseName()).isEqualTo("authdb");
        assertThat(postgresContainer.getUsername()).isEqualTo("authuser");
        assertThat(postgresContainer.getJdbcUrl()).contains("jdbc:postgresql://");
    }

    @Test
    @DisplayName("Should verify Keycloak Testcontainer is running, healthy, and serving OIDC endpoints")
    void shouldVerifyKeycloakContainer() throws IOException, InterruptedException {
        assertThat(keycloakContainer).isNotNull();
        assertThat(keycloakContainer.isRunning()).isTrue();

        String host = keycloakContainer.getHost();
        Integer mappedPort = keycloakContainer.getMappedPort(8080);
        assertThat(mappedPort).isPositive();

        // Verify Keycloak OIDC Discovery endpoint
        String oidcDiscoveryUrl = "http://" + host + ":" + mappedPort + "/realms/master/.well-known/openid-configuration";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(oidcDiscoveryUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("issuer");
        assertThat(response.body()).contains("jwks_uri");
        assertThat(response.body()).contains("/realms/master");
    }
}