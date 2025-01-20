package com.example.SynchronyDemo;

import com.example.SynchronyDemo.model.User;
import com.example.SynchronyDemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class PerformanceRunner implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // First create some test users
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .email("user" + i + "@example.com")
                    .build();
            users.add(userService.saveOrUpdateUser(user));
        }

        // Now test with existing user IDs
        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // Sequential
        long startSeq = System.currentTimeMillis();
        for (Long userId : userIds) {
            userService.getUserById(userId);
        }
        long seqTime = System.currentTimeMillis() - startSeq;

        // Parallel
        long startPar = System.currentTimeMillis();
        List<CompletableFuture<User>> futures = new ArrayList<>();
        for (Long userId : userIds) {
            futures.add(userService.getUserAsync(userId));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long parTime = System.currentTimeMillis() - startPar;

        System.out.println("Sequential total time: " + seqTime + " ms");
        System.out.println("Parallel total time:   " + parTime + " ms");
    }
}
