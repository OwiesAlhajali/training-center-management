package com.trainingcenter.management.config;

import com.trainingcenter.management.entity.User;
import com.trainingcenter.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createIfMissing("admin", "admin@trainingcenter.com", "Admin@123", User.UserType.ADMIN);
        createIfMissing("student", "student@trainingcenter.com", "Student@123", User.UserType.STUDENT);
        createIfMissing("teacher", "teacher@trainingcenter.com", "Teacher@123", User.UserType.TEACHER);
    }

    private void createIfMissing(String username, String email, String rawPassword, User.UserType userType) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Skipped seeding user '{}' because email '{}' already exists", username, email);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setUserType(userType);
        user.setContactInfo("seeded-user");

        userRepository.save(user);
        log.info("Seeded user '{}' with type {}", username, userType);
    }
}

