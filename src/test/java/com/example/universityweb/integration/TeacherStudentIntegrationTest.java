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
public class TeacherStudentIntegrationTest {

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
    @WithUserDetails("prof.miller")
    @DisplayName("Integration Test 6: Authenticated teacher retrieves faculty list (HTTP 200)")
    void testGetAllTeachers_AsTeacher_Success() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].role", is("TEACHER")));
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("Integration Test 7: Admin creates new student account (HTTP 201)")
    void testCreateStudent_AsAdmin_Success() throws Exception {
        UserDto newStudent = UserDto.builder()
                .username("new.student")
                .password("studentPass123")
                .fullName("New Student")
                .email("new.student@university.edu")
                .role("STUDENT")
                .enabled(true)
                .build();

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("new.student")))
                .andExpect(jsonPath("$.role", is("STUDENT")));
    }

    @Test
    @WithUserDetails("alice.w")
    @DisplayName("Integration Test 8: Student is forbidden from deleting a teacher (HTTP 403)")
    void testDeleteTeacher_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/teachers/1"))
                .andExpect(status().isForbidden());
    }
}
