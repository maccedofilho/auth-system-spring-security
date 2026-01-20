package com.macedo.auth.authsystem.controller;

import com.macedo.auth.authsystem.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(
        name = "Usuário",
        description = "Endpoints para informações do usuário autenticado"
)
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    @GetMapping("/me")
    @Operation(
            summary = "Obter perfil do usuário autenticado",
            description = """
                    Retorna informações sobre o usuário atualmente autenticado.

                    **Informações Retornadas:**
                    * `user`: Email do usuário autenticado
                    * `authorities`: Lista de papéis/autorizações do usuário

                    **Autenticação:**
                    * Requer um Access Token válido no header Authorization
                    * Formato: `Authorization: Bearer <token>`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil do usuário retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado - Token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não tem permissão para acessar este recurso",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public Map<String, Object> me(
            @Parameter(
                    description = "Objeto de autenticação do Spring Security contendo informações do usuário",
                    hidden = true
            )
            Authentication auth
    ) {
        return Map.of(
                "user", auth.getName(),
                "authorities", auth.getAuthorities()
        );
    }
}
