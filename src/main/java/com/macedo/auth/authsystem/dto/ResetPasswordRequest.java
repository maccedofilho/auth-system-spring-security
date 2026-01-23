package com.macedo.auth.authsystem.dto;

import com.macedo.auth.authsystem.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "ResetPasswordRequest",
        description = "DTO para confirmação de reset de senha. Usa token recebido por email."
)
public class ResetPasswordRequest {

    @Schema(
            description = "Token de reset recebido por email",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            required = true
    )
    @NotBlank(message = "Token é obrigatório")
    private String token;

    @Schema(
            description = "Nova senha. Mínimo 12 caracteres, com maiúscula, minúscula, número e caractere especial.",
            example = "NovaSenha@456",
            required = true,
            format = "password",
            minLength = 12,
            maxLength = 100
    )
    @NotBlank(message = "Nova senha é obrigatória")
    @ValidPassword
    private String newPassword;
}
