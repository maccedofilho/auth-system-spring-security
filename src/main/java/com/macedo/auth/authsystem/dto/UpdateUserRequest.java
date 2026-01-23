package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "UpdateUserRequest",
        description = "DTO para atualização parcial do perfil do usuário autenticado"
)
public class UpdateUserRequest {

    @Schema(
            description = "Nome do usuário (2-120 caracteres)",
            example = "João Silva",
            minLength = 2,
            maxLength = 120
    )
    @Size(min = 2, max = 120, message = "Nome deve ter entre 2 e 120 caracteres")
    private String name;

    @Schema(
            description = "URL do avatar/foto de perfil",
            example = "https://example.com/avatar.jpg",
            maxLength = 500
    )
    @Size(max = 500, message = "URL do avatar deve ter no máximo 500 caracteres")
    private String avatarUrl;

    @Schema(
            description = "Número de telefone",
            example = "+5511987654321",
            maxLength = 20
    )
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phoneNumber;
}
