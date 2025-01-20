package com.example.SynchronyDemo.service;

import com.example.SynchronyDemo.model.User;
import com.example.SynchronyDemo.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getUserById_WhenInCache_ReturnsFromCache() {
        // Arrange
        User cachedUser = User.builder().id(1L).username("test").build();
        when(valueOperations.get("user:1")).thenReturn(cachedUser);

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(cachedUser.getId(), result.getId());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserById_WhenNotInCache_ReturnsFromDB() {
        // Arrange
        User dbUser = User.builder().id(1L).username("test").build();
        when(valueOperations.get("user:1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(dbUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(dbUser.getId(), result.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserAsync_ReturnsCompletableFuture() {
        // Arrange
        User user = User.builder().id(1L).username("test").build();
        when(valueOperations.get("user:1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        CompletableFuture<User> future = userService.getUserAsync(1L);

        // Assert
        assertNotNull(future);
        User result = future.join();
        assertEquals(user.getId(), result.getId());
    }
} 