package com.macedo.auth.authsystem.controller;

import com.macedo.auth.authsystem.dto.ErrorResponse;
import com.macedo.auth.authsystem.dto.UserResponse;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(
        name = "Administração",
        description = "Endpoints exclusivos para administradores do sistema (ROLE_ADMIN)"
)
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @Operation(
            summary = "Listar todos os usuários",
            description = """
                    Retorna uma lista de todos os usuários cadastrados no sistema.

                    **Informações retornadas:**
                    * ID, nome e email
                    * Roles (papeis) atribuídos
                    * Status da conta (ativo/inativo)
                    * Data de criação

                    **Nota:** Informações sensíveis como senhas nunca são retornadas.

                    **Permissão:**
                    * Requer ROLE_ADMIN
                    * Usuários com ROLE_USER receberão HTTP 403
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado - Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão - Requer ROLE_ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @Operation(
            summary = "Buscar usuário por ID",
            description = """
                    Retorna os detalhes de um usuário específico.

                    **Permissão:**
                    * Requer ROLE_ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID do usuário", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
