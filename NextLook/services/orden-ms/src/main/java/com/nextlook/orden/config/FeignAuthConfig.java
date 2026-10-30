package com.nextlook.orden.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.slf4j.MDC;

/**
 * S07 - Propagacion de identidad: cada llamada Feign lleva el mismo JWT con el que entro la peticion,
 * asi cliente-ms vuelve a validarlo (segunda linea de defensa) y sabe quien es el comprador.
 * Tambien propaga el X-Trace-ID para seguir la peticion entre servicios.
 */
@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor propagarTokenYTrace() {
        return template -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                template.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAuth.getToken().getTokenValue());
            }
            String traceId = MDC.get("traceId");
            if (traceId != null) {
                template.header("X-Trace-ID", traceId);
            }
        };
    }
}
