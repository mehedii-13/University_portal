package com.example.universityweb.service;

import com.example.universityweb.dto.UserDto;
import com.example.universityweb.model.Role;
import com.example.universityweb.model.User;
import com.example.universityweb.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    private Map<Long, User> userDatabase;
    private UserDto sampleDto;

    @BeforeEach
    void setUp() {
        userDatabase = new HashMap<>();

        sampleDto = UserDto.builder()
                .username("test.user")
                .password("raw_pass")
                .fullName("Test User")
                .email("test@university.edu")
                .role("TEACHER")
                .enabled(true)
                .build();

        passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "encoded_" + rawPassword;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return ("encoded_" + rawPassword).equals(encodedPassword);
            }
        };

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        String name = method.getName();
                        if ("existsByUsername".equals(name)) {
                            String u = (String) args[0];
                            return userDatabase.values().stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(u));
                        } else if ("existsByEmail".equals(name)) {
                            String e = (String) args[0];
                            return userDatabase.values().stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(e));
                        } else if ("existsById".equals(name)) {
                            Long id = (Long) args[0];
                            return userDatabase.containsKey(id);
                        } else if ("findById".equals(name)) {
                            Long id = (Long) args[0];
                            return Optional.ofNullable(userDatabase.get(id));
                        } else if ("findByUsername".equals(name)) {
                            String u = (String) args[0];
                            return userDatabase.values().stream().filter(user -> user.getUsername().equalsIgnoreCase(u)).findFirst();
                        } else if ("save".equals(name)) {
                            User u = (User) args[0];
                            if (u.getId() == null) {
                                u.setId((long) (userDatabase.size() + 1));
                            }
                            userDatabase.put(u.getId(), u);
                            return u;
                        } else if ("deleteById".equals(name)) {
                            Long id = (Long) args[0];
                            userDatabase.remove(id);
                            return null;
                        } else if ("findAll".equals(name)) {
                            return new ArrayList<>(userDatabase.values());
                        }
                        return null;
                    }
                }
        );

        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Unit Test 1: Create user successfully when details are valid")
    void testCreateUser_Success() {
        UserDto created = userService.createUser(sampleDto);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("test.user", created.getUsername());
        assertEquals("test@university.edu", created.getEmail());
        assertEquals("TEACHER", created.getRole());
        assertTrue(userDatabase.containsKey(created.getId()));
    }

    @Test
    @DisplayName("Unit Test 2: Create user throws exception when username already exists")
    void testCreateUser_DuplicateUsername_ThrowsException() {
        userService.createUser(sampleDto);

        UserDto duplicateUser = UserDto.builder()
                .username("test.user")
                .password("different_pass")
                .fullName("Another User")
                .email("other@university.edu")
                .role("TEACHER")
                .build();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(duplicateUser));
        assertTrue(exception.getMessage().contains("Username already exists: test.user"));
    }

    @Test
    @DisplayName("Unit Test 3: Create user throws exception when email already exists")
    void testCreateUser_DuplicateEmail_ThrowsException() {
        userService.createUser(sampleDto);

        UserDto duplicateEmailUser = UserDto.builder()
                .username("unique.user")
                .password("pass123")
                .fullName("Different User")
                .email("test@university.edu")
                .role("STUDENT")
                .build();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(duplicateEmailUser));
        assertTrue(exception.getMessage().contains("Email already exists: test@university.edu"));
    }

    @Test
    @DisplayName("Unit Test 4: Get user by ID returns UserDto when user exists")
    void testGetUserById_Success() {
        User user = User.builder()
                .id(1L)
                .username("existing.user")
                .password("encoded_pass")
                .fullName("Existing User")
                .email("existing@university.edu")
                .role(Role.TEACHER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        userDatabase.put(1L, user);

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("existing.user", result.getUsername());
        assertEquals("Existing User", result.getFullName());
    }

    @Test
    @DisplayName("Unit Test 5: Get user by ID throws exception when user does not exist")
    void testGetUserById_NotFound_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
        assertTrue(exception.getMessage().contains("User not found with id: 999"));
    }

    @Test
    @DisplayName("Unit Test 6: Delete user throws exception when user ID does not exist")
    void testDeleteUser_NotFound_ThrowsException() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(999L));
        assertTrue(exception.getMessage().contains("User not found with id: 999"));
    }
}
