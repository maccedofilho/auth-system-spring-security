package com.macedo.auth.authsystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    public void sendPasswordResetEmail(String to, String token, String baseUrl) {
        String resetLink = buildResetLink(baseUrl, token);

        log.info("====================================");
        log.info("EMAIL DE RECUPERAÇÃO DE SENHA");
        log.info("====================================");
        log.info("Para: {}", to);
        log.info("Token: {}", token);
        log.info("Link de Reset: {}", resetLink);
        log.info("====================================");
        log.info("Enviando email de recuperação para: {}", to);
    }

    private String buildResetLink(String baseUrl, String token) {
        return "%s/api/auth/reset-password?token=%s".formatted(baseUrl, token);
    }

    @SuppressWarnings("unused")
    private String buildEmailTemplate(String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .button { display: inline-block; padding: 12px 24px; background: #007bff; color: white; text-decoration: none; border-radius: 4px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Recuperação de Senha</h2>
                        <p>Você solicitou a recuperação de sua senha.</p>
                        <p>Clique no botão abaixo para redefinir sua senha:</p>
                        <p><a href="%s" class="button">Redefinir Senha</a></p>
                        <p>Ou copie e cole este link no navegador:</p>
                        <p>%s</p>
                        <p><strong>Este link expira em 1 hora.</strong></p>
                        <p>Se você não solicitou esta recuperação, ignore este email.</p>
                    </div>
                </body>
                </html>
                """.formatted(resetLink, resetLink);
    }
}
