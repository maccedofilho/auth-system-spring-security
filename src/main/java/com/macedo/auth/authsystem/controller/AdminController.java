package com.macedo.auth.authsystem.controller;

import com.macedo.auth.authsystem.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class AdminController {

    @GetMapping("/users")
    @Operation(
            summary = "Listar todos os usuários",
            description = """
                    Retorna uma lista de todos os usuários cadastrados no sistema.

                    **Permissão:**
                    * Requer ROLE_ADMIN
                    * Usuários com ROLE_USER receberão HTTP 403
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuários retornada com sucesso"
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
    public String getUsers() {
        // TODO: Implementar listagem de usuários
        return "Endpoint em desenvolvimento";
    }
}
