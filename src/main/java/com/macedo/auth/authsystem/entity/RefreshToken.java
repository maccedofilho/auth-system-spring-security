package com.macedo.auth.authsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity @Table(name = "refresh_tokens", indexes = @Index(name="idx_refresh_token", columnList="token", unique = true))

public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 200, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked;

    @Column(length = 255)
    private String deviceName;

    @Column(length = 45)
    private String ip;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant lastUsedAt;

}
