package com.macedo.auth.authsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",
            "https://yourdomain.com"
    );

    private static final List<String> ALLOWED_METHODS = Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
    );

    private static final List<String> ALLOWED_HEADERS = Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
    );

    private static final List<String> EXPOSED_HEADERS = Arrays.asList(
            "Authorization",
            "Content-Type"
    );

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(ALLOWED_ORIGINS);

        configuration.setAllowedMethods(ALLOWED_METHODS);

        configuration.setAllowedHeaders(ALLOWED_HEADERS);

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(EXPOSED_HEADERS);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
