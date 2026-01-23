package com.macedo.auth.authsystem.service;

import com.macedo.auth.authsystem.config.JwtProperties;
import com.macedo.auth.authsystem.dto.*;
import com.macedo.auth.authsystem.entity.*;
import com.macedo.auth.authsystem.exception.AccountLockedException;
import com.macedo.auth.authsystem.exception.EmailAlreadyExistsException;
import com.macedo.auth.authsystem.exception.InvalidCredentialsException;
import com.macedo.auth.authsystem.repository.RoleRepository;
import com.macedo.auth.authsystem.repository.UserRepository;
import com.macedo.auth.authsystem.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;
    private final JwtProperties props;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserRepository users, RoleRepository roles,
                       PasswordEncoder encoder, JwtTokenProvider jwt, JwtProperties props,
                       RefreshTokenService refreshTokenService,
                       LoginAttemptService loginAttemptService) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
        this.jwt = jwt;
        this.props = props;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional
    public void register(RegisterRequest req) {
        if (users.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }
        Role roleUser = roles.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roles.save(Role.builder().name(RoleName.ROLE_USER).build()));

        User u = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .roles(Set.of(roleUser))
                .enabled(true)
                .build();

        users.save(u);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        if (loginAttemptService.isLocked(req.getEmail())) {
            long remaining = loginAttemptService.getLockoutTimeRemaining(req.getEmail());
            throw new AccountLockedException("Account is temporarily locked due to multiple failed login attempts", remaining);
        }

        User u = users.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    loginAttemptService.loginFailed(req.getEmail());
                    return new InvalidCredentialsException("Invalid credentials");
                });

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            loginAttemptService.loginFailed(req.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        loginAttemptService.loginSucceeded(req.getEmail());

        String access = jwt.generateAccessToken(u.getEmail());
        String refreshToken = refreshTokenService.issue(u);

        AuthResponse resp = new AuthResponse();
        resp.setAccessToken(access);
        resp.setRefreshToken(refreshToken);
        resp.setExpiresIn(props.getAccessTokenExpirationMs());
        return resp;
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest req) {
        var rt = refreshTokenService.validateAndGetRefreshToken(req.getRefreshToken());

        String newAccessToken = jwt.generateAccessToken(rt.getUser().getEmail());
        String newRefreshToken = refreshTokenService.refresh(req.getRefreshToken());

        return RefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(props.getAccessTokenExpirationMs())
                .build();
    }

    @Transactional
    public void logout(RefreshRequest req) {
        refreshTokenService.revoke(req.getRefreshToken());
    }

    @Transactional
    public void logoutAll(String email) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        refreshTokenService.revokeAll(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!encoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (req.getCurrentPassword().equals(req.getNewPassword())) {
            throw new InvalidCredentialsException("New password must be different from current password");
        }

        user.setPassword(encoder.encode(req.getNewPassword()));
        users.save(user);
        refreshTokenService.revokeAll(user);

        log.info("Password changed successfully for user: {}. All sessions revoked.", email);
    }
}
