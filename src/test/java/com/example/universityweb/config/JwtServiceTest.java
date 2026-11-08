package com.example.universityweb.config;

import com.example.universityweb.model.Role;
import com.example.universityweb.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // 256-bit secret key in Base64
        String base64Secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337339792442264529482B4D6251655468576D5A7134743677397A24432646294A";
        ReflectionTestUtils.setField(jwtService, "secretKey", base64Secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour

        testUser = User.builder()
                .id(10L)
                .username("jwt.user")
                .password("secret")
                .fullName("JWT User")
                .email("jwt@university.edu")
                .role(Role.STUDENT)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Unit Test: Generate token, extract claims, and validate successfully")
    void testGenerateAndValidateToken_Success() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String username = jwtService.extractUsername(token);
        assertEquals("jwt.user", username);

        boolean isValid = jwtService.isTokenValid(token, testUser);
        assertTrue(isValid);
    }
}

