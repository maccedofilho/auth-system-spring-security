package com.macedo.auth.authsystem.service;

import com.macedo.auth.authsystem.dto.PagedResponse;
import com.macedo.auth.authsystem.dto.UpdateUserRequest;
import com.macedo.auth.authsystem.dto.UserProfileResponse;
import com.macedo.auth.authsystem.dto.UserResponse;
import com.macedo.auth.authsystem.entity.Role;
import com.macedo.auth.authsystem.entity.User;
import com.macedo.auth.authsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    public UserProfileResponse updateProfile(String email, UpdateUserRequest request) {
        User user = findByEmail(email);

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    public PagedResponse<UserResponse> searchUsers(String query, Integer page, Integer size, String[] sort) {
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        int pageNumber = page != null && page >= 0 ? page : 0;

        Sort sortConfig = buildSort(sort);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortConfig);

        Page<User> userPage;
        if (query != null && !query.isBlank()) {
            userPage = userRepository.searchByNameOrEmail(query, pageable);
            log.info("Admin search: query='{}', page={}, size={}, results={}", query, pageNumber, pageSize, userPage.getTotalElements());
        } else {
            userPage = userRepository.findAll(pageable);
            log.info("Admin list users: page={}, size={}, total={}", pageNumber, pageSize, userPage.getTotalElements());
        }

        List<UserResponse> items = userPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(items, userPage.getNumber(), userPage.getSize(), userPage.getTotalElements());
    }

    private Sort buildSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Sort.Direction direction = Sort.Direction.ASC;
        String property = "createdAt";

        for (String sortParam : sort) {
            String[] parts = sortParam.split(",");
            property = parts[0];
            if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) {
                direction = Sort.Direction.DESC;
            } else {
                direction = Sort.Direction.ASC;
            }
        }

        return Sort.by(direction, property);
    }
}
