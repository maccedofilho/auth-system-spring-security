package com.macedo.auth.authsystem.repository;

import com.macedo.auth.authsystem.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    List<PasswordResetToken> findByEmailAndUsedFalseAndExpiryDateAfter(String email, Instant expiryDate);

    void deleteByExpiryDateBefore(Instant expiryDate);
}
