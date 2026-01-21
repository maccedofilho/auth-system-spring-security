package com.macedo.auth.authsystem.service;

import com.macedo.auth.authsystem.dto.UserResponse;
import com.macedo.auth.authsystem.entity.Role;
import com.macedo.auth.authsystem.entity.User;
import com.macedo.auth.authsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: " + email));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
