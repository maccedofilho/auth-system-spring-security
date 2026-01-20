package com.macedo.auth.authsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
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
                // Informações gerais da API
                .info(new Info()
                        .title("Auth System API")
                        .version("1.0.0")
                        .description("""
                                ### Sistema de Autenticação JWT com Spring Security

                                API completa para autenticação e autorização utilizando JWT (JSON Web Tokens).

                                ## Funcionalidades Principais

                                * **Registro de Usuários**: Crie novas contas com email e senha
                                * **Login**: Autentique-se e receba tokens de acesso
                                * **Refresh Token**: Renove seu token de acesso sem fazer login novamente
                                * **Perfil de Usuário**: Acesse informações do seu perfil
                                * **Administração**: Endpoints exclusivos para administradores

                                ## Segurança

                                * Tokens JWT com assinatura HS512
                                * Refresh tokens armazenados de forma segura (hash)
                                * Rate limiting em endpoints críticos
                                * Validação robusta de entrada
                                * Proteção contra ataques comuns (CSRF, XSS, etc.)

                                ## Autenticação

                                Para acessar endpoints protegidos, inclua o token no header Authorization:
                                ```
                                Authorization: Bearer <seu_token_aqui>
                                ```
                                """)
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@company.com")
                                .url("https://github.com/yourrepo/auth-system"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))

                // Servidores
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.yourdomain.com")
                                .description("Servidor de Produção")
                ))

                // Requisito de segurança global
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))

                // Componentes - Schemes de segurança
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                Autenticação JWT usando tokens Bearer.

                                                **Como obter o token:**
                                                1. Faça login via `POST /api/auth/login`
                                                2. Copie o `accessToken` da resposta
                                                3. Cole no campo acima (sem o prefixo 'Bearer')

                                                **Exemplo:**
                                                ```
                                                eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiw...
                                                ```

                                                **Nota:** O token expira em 15 minutos. Use o endpoint de refresh para renová-lo.
                                                """)));
    }
}
