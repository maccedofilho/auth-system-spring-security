package com.macedo.auth.authsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth System API")
                        .version("1.0.0")
                        .description("""
                                ## Sistema de Autenticação JWT com Spring Security

                                API completa para autenticação e gerenciamento de usuários com tokens JWT.

                                ### Funcionalidades Principais
                                - **Autenticação JWT**: Access tokens de curta duração (15 min)
                                - **Refresh Tokens**: Tokens de renovação com rotação automática (7 dias)
                                - **Rate Limiting**: Proteção contra ataques de força bruta
                                - **Role-Based Access Control**: RBAC com ROLE_USER e ROLE_ADMIN

                                ### Como Usar

                                1. **Registrar**: `POST /api/auth/register` - Crie uma conta
                                2. **Login**: `POST /api/auth/login` - Obtenha seus tokens
                                3. **Access Token**: Use no header `Authorization: Bearer {token}`
                                4. **Refresh Token**: `POST /api/auth/refresh` - Renove o access token expirado

                                ### Rate Limiting

                                | Endpoint | Limite | Período |
                                |----------|--------|---------|
                                | /api/auth/login | 5 | 1 minuto |
                                | /api/auth/register | 3 | 1 minuto |
                                | /api/auth/refresh | 10 | 1 minuto |

                                ### Códigos de Erro

                                | Código | Descrição |
                                |--------|-----------|
                                | 400 | Bad Request - Dados inválidos |
                                | 401 | Unauthorized - Não autenticado |
                                | 403 | Forbidden - Sem permissão |
                                | 404 | Not Found - Recurso não encontrado |
                                | 409 | Conflict - Email já existe |
                                | 429 | Too Many Requests - Rate limit excedido |
                                | 500 | Internal Server Error - Erro no servidor |
                                """)
                        .contact(new Contact()
                                .name("Auth System Team")
                                .email("contact@authsystem.com")
                                .url("https://github.com/macedo/auth-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://staging-api.authsystem.com")
                                .description("Servidor de Staging"),
                        new Server()
                                .url("https://api.authsystem.com")
                                .description("Servidor de Produção")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                Insira o token JWT obtido no endpoint `/api/auth/login`.

                                                Formato: `Bearer eyJhbGciOiJIUzUxMiJ9...`

                                                O access token expira em 15 minutos. Use o refresh token para obter um novo access token.
                                                """))
                );
    }
}
