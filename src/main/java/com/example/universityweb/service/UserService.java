package com.example.universityweb.service;

import com.example.universityweb.dto.UserDto;
import com.example.universityweb.model.Role;
import com.example.universityweb.model.User;
import com.example.universityweb.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toDto(user);
    }

    public List<UserDto> getUsersByRole(String role) {
        Role r = Role.valueOf(role.toUpperCase());
        return userRepository.findByRole(r).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists: " + dto.getEmail());
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .role(Role.valueOf(dto.getRole().toUpperCase()))
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (dto.getEmail() != null && !user.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email already exists: " + dto.getEmail());
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }

        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }
}
