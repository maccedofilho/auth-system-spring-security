package com.macedo.auth.authsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "password_reset_tokens",
        indexes = {
            @Index(name = "idx_password_reset_token", columnList = "token", unique = true),
            @Index(name = "idx_password_reset_email", columnList = "email")
        }
)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 255, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant usedAt;

    @Column(length = 45)
    private String ipAddress;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
