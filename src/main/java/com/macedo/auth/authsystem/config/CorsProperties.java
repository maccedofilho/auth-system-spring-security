package com.macedo.auth.authsystem.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Getter
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    private String allowedOrigins = "http://localhost:3000";

    private String allowedHeaders = "Authorization,Content-Type,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers";

    private String exposedHeaders = "Authorization,Content-Type";

    private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";

    private boolean allowCredentials = true;

    private long maxAge = 3600L;

    public List<String> getAllowedOriginsList() {
        return parseList(allowedOrigins);
    }

    public List<String> getAllowedHeadersList() {
        return parseList(allowedHeaders);
    }

    public List<String> getExposedHeadersList() {
        return parseList(exposedHeaders);
    }

    public List<String> getAllowedMethodsList() {
        return parseList(allowedMethods);
    }

    private List<String> parseList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public void setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public void setExposedHeaders(String exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public void setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods;
    }
}
