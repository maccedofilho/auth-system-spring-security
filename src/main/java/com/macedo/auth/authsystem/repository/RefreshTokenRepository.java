package com.macedo.auth.authsystem.repository;

import com.macedo.auth.authsystem.entity.RefreshToken;
import com.macedo.auth.authsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByIdAndUser(Long id, User user);
    void deleteByUser(User user);
    List<RefreshToken> findByUserAndRevokedFalseOrderByCreatedAtDesc(User user);
}
