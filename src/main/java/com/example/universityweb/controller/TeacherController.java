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
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllTeachers() {
        return ResponseEntity.ok(userService.getUsersByRole("TEACHER"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeacherById(@PathVariable Long id) {
        try {
            UserDto teacher = userService.getUserById(id);
            if (!"TEACHER".equalsIgnoreCase(teacher.getRole())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Teacher not found with id: " + id));
            }
            return ResponseEntity.ok(teacher);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTeacher(@Valid @RequestBody UserDto dto) {
        try {
            dto.setRole("TEACHER");
            UserDto created = userService.createUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @Valid @RequestBody UserDto dto,
                                           Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();

            // Teachers can only update their own profile, Admin can update anyone
            if (!currentUser.getRole().name().equals("ADMIN") && !currentUser.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only update your own profile"));
            }

            UserDto existing = userService.getUserById(id);
            if (!"TEACHER".equalsIgnoreCase(existing.getRole())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Teacher not found with id: " + id));
            }
            dto.setRole("TEACHER");
            UserDto updated = userService.updateUser(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        try {
            UserDto existing = userService.getUserById(id);
            if (!"TEACHER".equalsIgnoreCase(existing.getRole())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Teacher not found with id: " + id));
            }
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Teacher deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
