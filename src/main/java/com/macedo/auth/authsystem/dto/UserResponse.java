package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "UserResponse",
        description = "Representação de um usuário para resposta da API (sem informações sensíveis)"
)
public class UserResponse {

    @Schema(
            description = "ID único do usuário",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Nome completo do usuário",
            example = "Ricardo Macedo"
    )
    private String name;

    @Schema(
            description = "Email do usuário",
            example = "ricardo.macedo@example.com"
    )
    private String email;

    @Schema(
            description = "Papéis (roles) atribuídos ao usuário",
            example = "[\"ROLE_USER\"]"
    )
    private Set<String> roles;

    @Schema(
            description = "Indica se a conta está ativa",
            example = "true"
    )
    private boolean enabled;

    @Schema(
            description = "Data e hora de criação da conta",
            example = "2025-01-21T10:30:00Z"
    )
    private Instant createdAt;
}
