package com.macedo.auth.authsystem.controller;

import com.macedo.auth.authsystem.dto.ErrorResponse;
import com.macedo.auth.authsystem.dto.SessionResponse;
import com.macedo.auth.authsystem.service.RefreshTokenService;
import com.macedo.auth.authsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(
        name = "Usuário",
        description = "Endpoints para informações do usuário autenticado"
)
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class UserController {

    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

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

    @GetMapping("/sessions")
    @Operation(
            summary = "Listar sessões ativas",
            description = """
                    Retorna todas as sessões ativas do usuário autenticado.

                    **Informações Retornadas:**
                    * Identificador único da sessão
                    * Nome do dispositivo (quando disponível)
                    * Endereço IP da sessão
                    * User Agent do navegador/aplicativo
                    * Data de criação e último uso
                    * Indicador de sessão atual

                    **Uso:**
                    * Identificar logins suspeitos
                    * Gerenciar sessões em múltiplos dispositivos
                    * Base para revogação de sessão específica
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de sessões ativas retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public List<SessionResponse> sessions(
            @Parameter(
                    description = "Token de refresh atual (opcional, via query param para identificar sessão atual)",
                    required = false
            )
            @RequestParam(required = false) String refreshToken,
            Authentication auth
    ) {
        var user = userService.findByEmail(auth.getName());
        return refreshTokenService.getSessionsByUser(user, refreshToken);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(
            summary = "Revogar sessão específica",
            description = """
                    Revoga uma sessão específica do usuário autenticado.

                    **Comportamento:**
                    * Apenas a sessão especificada é revogada
                    * Outras sessões do usuário permanecem ativas
                    * Operação é idempotente - chamadas repetidas para mesma sessão não geram erro
                    * Sessão revogada não pode mais obter novos access tokens

                    **Segurança:**
                    * Só é possível revogar sessões do próprio usuário autenticado
                    * Tentativa de revogar sessão de outro usuário retorna 404

                    **Casos de Uso:**
                    * Identificou sessão suspeita em outro dispositivo
                    * Saiu de um dispositivo específico
                    * Controle fino de sessões ativas
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Sessão revogada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sessão não encontrada ou não pertence ao usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> revokeSession(
            @Parameter(
                    description = "ID da sessão a ser revogada",
                    required = true,
                    example = "123"
            )
            @PathVariable Long sessionId,
            Authentication auth
    ) {
        var user = userService.findByEmail(auth.getName());
        refreshTokenService.revokeSessionById(sessionId, user);
        return ResponseEntity.noContent().build();
    }
}
