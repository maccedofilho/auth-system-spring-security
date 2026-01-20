package com.macedo.auth.authsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ErrorResponse",
        description = "DTO padrão de resposta de erro. Retornado em caso de falha em requisições à API."
)
public class ErrorResponse {

    @Schema(
            description = "Código do erro para identificação programática",
            example = "INVALID_CREDENTIALS"
    )
    private String code;

    @Schema(
            description = "Mensagem descritiva do erro em formato legível para humanos",
            example = "Email ou senha incorretos"
    )
    private String message;

    @Schema(
            description = "Timestamp de quando o erro ocorreu, em UTC",
            example = "2024-01-17T12:34:56.789Z",
            type = "string",
            format = "date-time"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    @Schema(
            description = "Caminho da requisição que gerou o erro",
            example = "/api/auth/login"
    )
    private String path;
}
