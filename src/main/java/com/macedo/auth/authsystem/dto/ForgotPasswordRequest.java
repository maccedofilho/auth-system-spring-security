package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "ForgotPasswordRequest",
        description = "DTO para solicitação de recuperação de senha. Envia email com token de reset."
)
public class ForgotPasswordRequest {

    @Schema(
            description = "Email do usuário para recuperar a senha",
            example = "joao.silva@example.com",
            required = true
    )
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;
}
