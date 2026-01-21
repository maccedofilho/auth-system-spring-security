package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "SessionResponse",
        description = "DTO representando uma sessão ativa do usuário"
)
public class SessionResponse {

    @Schema(
            description = "Identificador único da sessão",
            example = "1"
    )
    private Long sessionId;

    @Schema(
            description = "Nome do dispositivo (quando disponível)",
            example = "Chrome on Windows"
    )
    private String deviceName;

    @Schema(
            description = "Endereço IP usado na criação da sessão",
            example = "192.168.1.100"
    )
    private String ip;

    @Schema(
            description = "User Agent do navegador/aplicativo",
            example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    )
    private String userAgent;

    @Schema(
            description = "Data e hora de criação da sessão",
            example = "2024-01-15T10:30:00Z"
    )
    private Instant createdAt;

    @Schema(
            description = "Data e hora do último uso da sessão",
            example = "2024-01-15T14:45:00Z"
    )
    private Instant lastUsedAt;

    @Schema(
            description = "Indica se esta é a sessão atual (usada nesta requisição)",
            example = "true"
    )
    private boolean isCurrent;

    @Schema(
            description = "Data e hora de expiração da sessão",
            example = "2024-01-22T10:30:00Z"
    )
    private Instant expiresAt;
}
