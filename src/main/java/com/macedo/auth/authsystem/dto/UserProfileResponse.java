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
        name = "UserProfileResponse",
        description = "Perfil completo do usuário autenticado"
)
public class UserProfileResponse {

    @Schema(
            description = "ID único do usuário",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Nome completo do usuário",
            example = "João Silva"
    )
    private String name;

    @Schema(
            description = "Email do usuário",
            example = "joao.silva@example.com"
    )
    private String email;

    @Schema(
            description = "URL do avatar/foto de perfil",
            example = "https://example.com/avatar.jpg"
    )
    private String avatarUrl;

    @Schema(
            description = "Número de telefone",
            example = "+5511987654321"
    )
    private String phoneNumber;

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

    @Schema(
            description = "Data e hora da última atualização",
            example = "2025-01-22T15:45:00Z"
    )
    private Instant updatedAt;
}
