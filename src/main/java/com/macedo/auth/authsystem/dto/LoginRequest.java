package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "LoginRequest",
        description = "DTO para solicitação de login no sistema. Contém as credenciais do usuário para autenticação."
)
public class LoginRequest {

    @Schema(
            description = "Email do usuário cadastrado no sistema",
            example = "joao.silva@example.com",
            required = true
    )
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @Schema(
            description = "Senha do usuário. Deve ter entre 8 e 100 caracteres",
            example = "MinhaSenha@123",
            required = true,
            format = "password",
            minLength = 8,
            maxLength = 100
    )
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    private String password;
}
