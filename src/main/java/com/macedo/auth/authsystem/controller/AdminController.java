package com.macedo.auth.authsystem.controller;

import com.macedo.auth.authsystem.dto.ErrorResponse;
import com.macedo.auth.authsystem.dto.PagedResponse;
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
            summary = "Listar usuários com paginação e busca",
            description = """
                    Retorna lista paginada de usuários com suporte a busca e ordenação.

                    **Parâmetros:**
                    * `page`: Número da página (0-indexed, default=0)
                    * `size`: Itens por página (default=20, max=100)
                    * `sort`: Ordenação (ex: createdAt,desc ou name,asc)
                    * `q`: Busca por nome ou email

                    **Resposta:**
                    * `items`: Lista de usuários da página atual
                    * `page`: Metadados de paginação (number, size, totalItems, totalPages)

                    **Ordenação padrão:** createdAt,desc (mais recentes primeiro)

                    **Permissão:** Requer ROLE_ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão - Requer ROLE_ADMIN",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @Parameter(description = "Número da página (0-indexed)", example = "0")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Itens por página (max=100)", example = "20")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "Ordenação (ex: createdAt,desc ou name,asc)", example = "createdAt,desc")
            @RequestParam(required = false) String[] sort,
            @Parameter(description = "Busca por nome ou email", example = "joao")
            @RequestParam(required = false, name = "q") String query
    ) {
        return ResponseEntity.ok(userService.searchUsers(query, page, size, sort));
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
