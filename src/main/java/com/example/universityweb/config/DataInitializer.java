package com.example.universityweb.config;

import com.example.universityweb.model.Role;
import com.example.universityweb.model.User;
import com.example.universityweb.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Administrators
        getOrCreateUser("admin", "admin123", "Arthur Pendelton", "arthur.pendelton@university.edu", Role.ADMIN);
        getOrCreateUser("registrar", "reg123", "Eleanor Vance", "eleanor.vance@university.edu", Role.ADMIN);

        // 2. Teachers / Faculty
        getOrCreateUser("prof.miller", "miller123", "Prof. Robert Miller", "robert.miller@university.edu", Role.TEACHER);
        getOrCreateUser("dr.zhao", "zhao123", "Dr. Lin Zhao", "lin.zhao@university.edu", Role.TEACHER);
        getOrCreateUser("prof.patel", "patel123", "Prof. Priya Patel", "priya.patel@university.edu", Role.TEACHER);
        getOrCreateUser("dr.ross", "ross123", "Dr. David Ross", "david.ross@university.edu", Role.TEACHER);

        // 3. Students
        getOrCreateUser("alice.w", "alice123", "Alice Walker", "alice.walker@university.edu", Role.STUDENT);
        getOrCreateUser("david.k", "david123", "David Kim", "david.kim@university.edu", Role.STUDENT);
        getOrCreateUser("clara.b", "clara123", "Clara Bennett", "clara.bennett@university.edu", Role.STUDENT);
        getOrCreateUser("liam.o", "liam123", "Liam O'Connor", "liam.oconnor@university.edu", Role.STUDENT);
        getOrCreateUser("maya.s", "maya123", "Maya Sharma", "maya.sharma@university.edu", Role.STUDENT);

        log.info("Data initialization complete. Total users seeded: {}", userRepository.count());
    }

    private User getOrCreateUser(String username, String rawPassword, String fullName, String email, Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .email(email)
                    .role(role)
                    .enabled(true)
                    .build();
            User saved = userRepository.save(user);
            log.info("Created user: {} ({})", username, role);
            return saved;
        });
    }
}
