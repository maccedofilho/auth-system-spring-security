package com.macedo.auth.authsystem.controller;

import com.macedo.auth.authsystem.dto.*;
import com.macedo.auth.authsystem.service.AuthService;
import com.macedo.auth.authsystem.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Autenticação",
        description = "Endpoints para autenticação, registro e gerenciamento de tokens JWT"
)
public class AuthController {

    private final AuthService auth;
    private final PasswordResetService passwordReset;

    public AuthController(AuthService auth, PasswordResetService passwordReset) {
        this.auth = auth;
        this.passwordReset = passwordReset;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar novo usuário",
            description = """
                    Cria uma nova conta de usuário no sistema.

                    **Regras de Validação:**
                    * Nome: 2-120 caracteres
                    * Email: deve ser válido e único
                    * Senha: 8-100 caracteres

                    **Após o registro:**
                    * A conta é criada com status 'enabled'
                    * A senha é armazenada de forma criptografada (BCrypt)
                    * Um papel padrão (ROLE_USER) é atribuído automaticamente
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email já cadastrado no sistema",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Muitas tentativas de registro (rate limit)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        auth.register(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário",
            description = """
                    Realiza a autenticação do usuário e retorna tokens JWT.

                    **Funcionamento:**
                    1. Valida as credenciais (email e senha)
                    2. Gera um Access Token (válido por 15 minutos)
                    3. Gera um Refresh Token (válido por 7 dias)
                    4. Armazena o Refresh Token de forma segura (hash) no banco

                    **Uso dos Tokens:**
                    * **Access Token**: Use para acessar endpoints protegidos
                    * **Refresh Token**: Use para obter um novo Access Token sem fazer login novamente

                    **Rate Limiting:**
                    * Máximo 5 tentativas por minuto por IP
                    * Após exceder, receba HTTP 429
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas (email ou senha incorretos)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Conta desabilitada ou bloqueada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Muitas tentativas de login (rate limit)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(auth.login(req));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Renovar token de acesso",
            description = """
                    Renova o Access Token usando um Refresh Token válido.

                    **Funcionamento:**
                    1. Valida o Refresh Token fornecido
                    2. Gera um novo Access Token (15 minutos)
                    3. Gera um novo Refresh Token (7 dias)
                    4. Invalida o Refresh Token anterior (rotação de token)
                    5. Atualiza o hash no banco de dados

                    **Segurança - Rotação de Token:**
                    * Cada refresh gera um novo par de tokens
                    * O token anterior é invalidado automaticamente
                    * Previne reuso de tokens comprometidos

                    **Rate Limiting:**
                    * Máximo 10 requisições por minuto por IP
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token renovado com sucesso",
                    content = @Content(schema = @Schema(implementation = RefreshResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token não encontrado ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Muitas requisições de refresh (rate limit)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(auth.refresh(req));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout do usuário",
            description = """
                    Revoga o refresh token e adiciona o access token à blacklist.

                    **Comportamento:**
                    * O refresh token fornecido é marcado como revogado no banco de dados
                    * O access token atual é adicionado à blacklist (não pode mais ser usado)
                    * Após revogado, o token não pode mais ser usado para obter novos access tokens
                    * Access token é invalidado imediatamente via blacklist

                    **Nota sobre Segurança:**
                    * O access token é invalidado imediatamente via JWT blacklist
                    * Tokens na blacklist expiram após 15 minutos
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout realizado com sucesso - tokens revogados"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido, expirado ou já revogado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req, HttpServletRequest request) {
        auth.logout(req);

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            auth.logoutWithToken(accessToken);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(
            summary = "Logout global - revoga todas as sessões",
            description = """
                    Revoga todos os refresh tokens do usuário autenticado, encerrando todas as sessões ativas.

                    **Comportamento:**
                    * Todos os refresh tokens do usuário são deletados do banco de dados
                    * O access token atual é adicionado à blacklist
                    * Após o logout global, nenhum dispositivo pode obter novos access tokens
                    * Access tokens antigos continuarão válidos até expirarem (ou serem invalidados via logout individual)

                    **Casos de Uso:**
                    * Suspeita de conta comprometida
                    * Troca de senha ou dispositivo
                    * Limpeza de sessões antigas
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout global realizado - todas as sessões foram encerradas"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado - token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> logoutAll(Authentication authentication, HttpServletRequest request) {
        auth.logoutAll(authentication.getName());

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            auth.logoutWithToken(accessToken);
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Alterar senha do usuário autenticado",
            description = """
                    Altera a senha do usuário autenticado após validar a senha atual.

                    **Comportamento:**
                    * Valida a senha atual fornecida
                    * Atualiza para a nova senha (armazenada com hash BCrypt)
                    * Revoga todos os refresh tokens do usuário (encerra todas as sessões)
                    * Usuário deve fazer login novamente após a troca

                    **Requisitos da Nova Senha:**
                    * Mínimo 8 caracteres
                    * Máximo 100 caracteres
                    * Deve ser diferente da senha atual

                    **Após a Troca:**
                    * Todos os dispositivos são desconectados
                    * Access tokens ainda válidos continuarão funcionando até expirarem (15 min)
                    * Login necessário com a nova senha

                    **Segurança:**
                    * Rate limit: 3 tentativas por minuto por usuário
                    * Log de auditoria registrado para cada troca
                    """
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Senha alterada com sucesso - todas as sessões foram encerradas"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou nova senha igual à atual",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado ou senha atual incorreta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Muitas tentativas de troca de senha (rate limit)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            Authentication authentication
    ) {
        auth.changePassword(authentication.getName(), req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Solicitar recuperação de senha",
            description = """
                    Inicia o fluxo de recuperação de senha enviando um email com token de reset.

                    **Comportamento:**
                    * Sempre retorna 204 No Content, mesmo que o email não exista (segurança)
                    * Um token único é gerado e enviado para o email
                    * O token expira em 1 hora
                    * Token é de uso único

                    **Rate Limiting:**
                    * Máximo 2 solicitações por hora por IP
                    * Proteção contra abuso e enumeração de emails

                    **Nota:**
                    * Em produção, o email é enviado com o link de reset
                    * Em desenvolvimento, o token é logado no console
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Solicitação processada. Se o email existir, um token foi enviado."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Muitas solicitações de recuperação (rate limit)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req,
            HttpServletRequest request
    ) {
        String ipAddress = getClientIp(request);
        passwordReset.initiatePasswordReset(req.getEmail(), ipAddress);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Redefinir senha com token",
            description = """
                    Redefine a senha do usuário usando o token recebido por email.

                    **Comportamento:**
                    * Valida o token (assinatura, expiração, uso único)
                    * Atualiza a senha com hash BCrypt
                    * Marca o token como usado
                    * Revoga todos os refresh tokens (encerra todas as sessões)

                    **Após o Reset:**
                    * Usuário deve fazer login novamente
                    * Todos os dispositivos são desconectados

                    **Rate Limiting:**
                    * Máximo 5 tentativas por hora por IP

                    **Erros Comuns:**
                    * 400: Token inválido, expirado ou já usado
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Senha redefinida com sucesso - todas as sessões foram encerradas"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token inválido, expirado, já usado ou nova senha inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Muitas tentativas de reset (rate limit)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        passwordReset.resetPassword(req);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
