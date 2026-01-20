package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "RefreshRequest",
        description = "DTO para solicitação de refresh token. Permite renovar o access token sem necessidade de novo login."
)
public class RefreshRequest {

    @Schema(
            description = "Token de refresh obtido no login ou refresh anterior. É válido por 7 dias",
            example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.q3P_-yZmVhK2ZGNnT8tF4vXgE9PkqY1WwRvBZJ7LdXc",
            required = true
    )
    @NotBlank(message = "Refresh token é obrigatório")
    private String refreshToken;
}
