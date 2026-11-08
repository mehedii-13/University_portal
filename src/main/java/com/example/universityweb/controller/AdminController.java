package com.example.universityweb.controller;

import com.example.universityweb.dto.UserDto;
import com.example.universityweb.model.User;
import com.example.universityweb.service.UserService;
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
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllAdmins() {
        return ResponseEntity.ok(userService.getUsersByRole("ADMIN"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Long id) {
        try {
            UserDto admin = userService.getUserById(id);
            if (!"ADMIN".equalsIgnoreCase(admin.getRole())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Admin not found with id: " + id));
            }
            return ResponseEntity.ok(admin);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createAdmin(@Valid @RequestBody UserDto dto) {
        try {
            dto.setRole("ADMIN");
            UserDto created = userService.createUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        try {
            UserDto existing = userService.getUserById(id);
            if (!"ADMIN".equalsIgnoreCase(existing.getRole())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Admin not found with id: " + id));
            }
            dto.setRole("ADMIN");
            UserDto updated = userService.updateUser(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser.getId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete your own admin account"));
            }
            UserDto existing = userService.getUserById(id);
            if (!"ADMIN".equalsIgnoreCase(existing.getRole())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Admin not found with id: " + id));
            }
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Admin deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
