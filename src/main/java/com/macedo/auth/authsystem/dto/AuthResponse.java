package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "AuthResponse",
        description = "DTO de resposta após login bem-sucedido. Contém os tokens de acesso e refresh."
)
public class AuthResponse {

    @Schema(
            description = "Token de acesso JWT (Access Token). Use este token para acessar endpoints protegidos",
            example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.q3P_-yZmVhK2ZGNnT8tF4vXgE9PkqY1WwRvBZJ7LdXc"
    )
    private String accessToken;

    @Schema(
            description = "Token de refresh JWT. Use para obter um novo access token quando este expirar",
            example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.q3P_-yZmVhK2ZGNnT8tF4vXgE9PkqY1WwRvBZJ7LdXc"
    )
    private String refreshToken;

    @Schema(
            description = "Tipo de token. Sempre 'Bearer' para este sistema",
            example = "Bearer",
            allowableValues = {"Bearer"}
    )
    private String tokenType = "Bearer";

    @Schema(
            description = "Tempo de expiração do access token em segundos (15 minutos = 900 segundos)",
            example = "900"
    )
    private long expiresIn;
}


