package com.example.universityweb.integration;

import com.example.universityweb.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class AdminControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("Integration Test 3: Admin gets list of administrators successfully (HTTP 200)")
    void testGetAllAdmins_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].role", is("ADMIN")));
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("Integration Test 4: Admin creates a new administrator account (HTTP 201)")
    void testCreateAdmin_AsAdmin_Success() throws Exception {
        UserDto newAdmin = UserDto.builder()
                .username("new.admin")
                .password("adminPass123")
                .fullName("New Administrator")
                .email("new.admin@university.edu")
                .role("ADMIN")
                .enabled(true)
                .build();

        mockMvc.perform(post("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAdmin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("new.admin")))
                .andExpect(jsonPath("$.email", is("new.admin@university.edu")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @WithUserDetails("alice.w")
    @DisplayName("Integration Test 5: Student is forbidden from accessing Admin endpoints (HTTP 403)")
    void testGetAdmins_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isForbidden());
    }
}
