package com.macedo.auth.authsystem.dto;

import com.macedo.auth.authsystem.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
            description = "Nova senha desejada. Mínimo 12 caracteres, com maiúscula, minúscula, número e caractere especial.",
            example = "NovaSenhaSegura@456",
            required = true,
            format = "password",
            minLength = 12,
            maxLength = 100
    )
    @NotBlank(message = "Nova senha é obrigatória")
    @ValidPassword
    private String newPassword;
}
