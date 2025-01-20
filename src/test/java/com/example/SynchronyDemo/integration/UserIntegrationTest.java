package com.example.SynchronyDemo.integration;

import com.example.SynchronyDemo.model.User;
import com.example.SynchronyDemo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.0-oracle")
                    .asCompatibleSubstituteFor("mysql"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withEnv("MYSQL_ROOT_PASSWORD", "test")
            .withStartupTimeout(Duration.ofMinutes(5))
            .withReuse(true);

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:6.2.5-alpine"))
            .withExposedPorts(6379)
            .withStartupTimeout(Duration.ofMinutes(2))
            .withReuse(true);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Test
    void createAndRetrieveUser() {
        // Clear any existing data
        userRepository.deleteAll();

        User user = User.builder()
                .username("integrationTest")
                .email("integration@test.com")
                .build();

        // Create user
        User created = restTemplate.postForObject(
                "http://localhost:" + port + "/api/users",
                user,
                User.class
        );
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(user.getUsername(), created.getUsername());

        // Retrieve user
        User retrieved = restTemplate.getForObject(
                "http://localhost:" + port + "/api/users/" + created.getId(),
                User.class
        );
        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
    }
} 