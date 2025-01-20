package com.example.SynchronyDemo.service;

import com.example.SynchronyDemo.model.User;
import com.example.SynchronyDemo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class UserServiceCacheTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:6.2.5-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void verifyCache() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .username("testUser")
                .email("test@example.com")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act - First call should hit the database
        userService.getUserById(1L);

        // Act - Second call should hit the cache
        userService.getUserById(1L);

        // Assert - Repository should only be called once
        verify(userRepository, times(1)).findById(1L);
    }
} 