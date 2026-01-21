package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "ChangePasswordRequest",
        description = "DTO para solicitação de troca de senha de usuário autenticado"
)
public class ChangePasswordRequest {

    @Schema(
            description = "Senha atual do usuário (para validação)",
            example = "SenhaAntiga@123",
            required = true,
            format = "password"
    )
    @NotBlank(message = "Senha atual é obrigatória")
    private String currentPassword;

    @Schema(
            description = "Nova senha desejada",
            example = "NovaSenhaSegura@456",
            required = true,
            format = "password",
            minLength = 8,
            maxLength = 100
    )
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, max = 100, message = "Nova senha deve ter entre 8 e 100 caracteres")
    private String newPassword;
}
