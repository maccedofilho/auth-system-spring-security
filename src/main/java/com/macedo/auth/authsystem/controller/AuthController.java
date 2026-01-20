package com.macedo.auth.authsystem.controller;

import com.macedo.auth.authsystem.dto.*;
import com.macedo.auth.authsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Autenticação",
        description = "Endpoints para autenticação, registro e gerenciamento de tokens JWT"
)
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
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
}
