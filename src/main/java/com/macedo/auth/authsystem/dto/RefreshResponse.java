package com.macedo.auth.authsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "RefreshResponse",
        description = "DTO de resposta após refresh de token bem-sucedido. Retorna novo access token e refresh token."
)
public class RefreshResponse {

    @Schema(
            description = "Novo token de acesso JWT. Substitua o access token anterior por este",
            example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.q3P_-yZmVhK2ZGNnT8tF4vXgE9PkqY1WwRvBZJ7LdXc"
    )
    private String accessToken;

    @Schema(
            description = "Novo token de refresh. O refresh token anterior é invalidado automaticamente (rotação de token)",
            example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.q3P_-yZmVhK2ZGNnT8tF4vXgE9PkqY1WwRvBZJ7LdXc"
    )
    private String refreshToken;

    @Schema(
            description = "Tipo de token. Sempre 'Bearer' para este sistema",
            example = "Bearer",
            allowableValues = {"Bearer"}
    )
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(
            description = "Tempo de expiração do novo access token em segundos (15 minutos = 900 segundos)",
            example = "900"
    )
    private long expiresIn;
}
