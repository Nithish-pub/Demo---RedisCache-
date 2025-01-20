package com.example.SynchronyDemo.service;

import com.example.SynchronyDemo.model.User;
import com.example.SynchronyDemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String USER_CACHE_PREFIX = "user:";

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    @Autowired
    public UserService(UserRepository userRepository, 
                      RedisTemplate<String, Object> redisTemplate,
                      MeterRegistry registry) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.cacheHitCounter = registry.counter("cache.hits");
        this.cacheMissCounter = registry.counter("cache.misses");
    }

    /**
     * Example of an asynchronous retrieval method. This is parallel-capable if multiple calls are made at once.
     */
    @Timed(value = "user.async.get", description = "Time taken to retrieve user asynchronously")
    @Async("asyncTaskExecutor")
    public CompletableFuture<User> getUserAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> getUserById(userId));
    }

    /**
     * Synchronous method: checks Redis first, if miss => fetch from DB => store in Redis.
     */
    @Timed(value = "user.sync.get", description = "Time taken to retrieve user synchronously")
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public User getUserById(Long userId) {
        String key = USER_CACHE_PREFIX + userId;
        try {
            // 1) Attempt to read from cache
            User cachedUser = (User) redisTemplate.opsForValue().get(key);
            if (cachedUser != null) {
                cacheHitCounter.increment();
                logger.info("Cache hit for user {}", userId);
                return cachedUser; // cache hit
            }
            cacheMissCounter.increment();
            logger.info("Cache miss for user {}", userId);
        } catch (Exception e) {
            // If Redis fails, we log it and continue with DB fallback
            logger.error("Redis unavailable, fallback to DB. Error: {}", e.getMessage(), e);
        }

        // 2) If cache miss, fetch from DB
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 3) Update cache
            try {
                redisTemplate.opsForValue().set(key, user);
            } catch (Exception e) {
                // If Redis set fails, just log
                System.err.println("Redis cache update failed: " + e.getMessage());
            }
            return user;
        }
        return null;
    }

    /**
     * Create or update user in DB, then sync to cache.
     */
    public User saveOrUpdateUser(User user) {
        User saved = userRepository.save(user);
        try {
            redisTemplate.opsForValue().set(USER_CACHE_PREFIX + saved.getId(), saved);
        } catch (Exception e) {
            // If Redis fails, we still keep DB changes
            System.err.println("Redis cache update failed for user " + saved.getId() + ": " + e.getMessage());
        }
        return saved;
    }

    /**
     * Delete user from DB, remove from cache.
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        try {
            redisTemplate.delete(USER_CACHE_PREFIX + userId);
        } catch (Exception e) {
            System.err.println("Redis cache delete failed: " + e.getMessage());
        }
    }
}
