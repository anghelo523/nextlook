package com.nextlook.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * S11 - CORS en UN solo lugar: el Gateway es la unica puerta por la que entra la SPA
 * (http://localhost:4200 llama a http://localhost:18080: distinto puerto = distinto origen).
 * Los microservicios no configuran CORS: si lo hicieran, el navegador recibiria el header
 * Access-Control-Allow-Origin dos veces y bloquearia la respuesta.
 * El Gateway es Spring Cloud Gateway Server WebMVC: CORS se configura con un bean, no con
 * spring.cloud.gateway.globalcors (eso es solo para la variante WebFlux).
 */
@Configuration
public class CorsConfig {

    @Value("${nextlook.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList());
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Trace-ID"));
        // Sin esto, el JavaScript de la SPA no puede leer el X-Trace-ID de la respuesta (S10)
        configuracion.setExposedHeaders(List.of("X-Trace-ID", "Location"));
        configuracion.setAllowCredentials(false); // el token va en Authorization, no en cookies
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuracion);
        return source;
    }
}
