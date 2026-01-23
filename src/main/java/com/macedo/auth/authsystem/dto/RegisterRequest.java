package com.macedo.auth.authsystem.dto;

import com.macedo.auth.authsystem.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "RegisterRequest",
        description = "DTO para solicitação de registro de novo usuário. Todos os campos são obrigatórios."
)
public class RegisterRequest {

    @Schema(
            description = "Nome completo do usuário",
            example = "João Silva",
            required = true,
            minLength = 2,
            maxLength = 120
    )
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 120, message = "Nome deve ter entre 2 e 120 caracteres")
    private String name;

    @Schema(
            description = "Email que será usado para login e contato. Deve ser único no sistema",
            example = "joao.silva@example.com",
            required = true
    )
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @Schema(
            description = "Senha para acesso ao sistema. Será armazenada de forma criptografada. Mínimo 12 caracteres, com maiúscula, minúscula, número e caractere especial.",
            example = "MinhaSenha@123",
            required = true,
            format = "password",
            minLength = 12,
            maxLength = 100
    )
    @NotBlank(message = "Senha é obrigatória")
    @ValidPassword
    private String password;
}
