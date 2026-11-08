package com.example.universityweb.controller;

import com.example.universityweb.dto.UserDto;
import com.example.universityweb.service.UserService;
import com.example.universityweb.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllStudents() {
        return ResponseEntity.ok(userService.getUsersByRole("STUDENT"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id) {
        try {
            UserDto student = userService.getUserById(id);
            if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Student not found with id: " + id));
            }
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createStudent(@Valid @RequestBody UserDto dto) {
        try {
            dto.setRole("STUDENT");
            UserDto created = userService.createUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody UserDto dto,
                                           Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();

            // Students can only update their own profile, Admin can update anyone
            if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only update your own profile"));
            }

            UserDto existing = userService.getUserById(id);
            if (!"STUDENT".equalsIgnoreCase(existing.getRole())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Student not found with id: " + id));
            }
            dto.setRole("STUDENT");
            UserDto updated = userService.updateUser(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            UserDto existing = userService.getUserById(id);
            if (!"STUDENT".equalsIgnoreCase(existing.getRole())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Student not found with id: " + id));
            }
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
