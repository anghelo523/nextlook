package com.nextlook.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * S07 - Primera linea de defensa: el Gateway valida el JWT de Keycloak (realm nextlook)
 * y autoriza por ruta, metodo y rol. Spring Security aplica la PRIMERA regla que coincide.
 * Cada microservicio vuelve a validar el token por su cuenta (segunda linea).
 */
@Configuration
public class SecurityConfig {

    /** Traduce realm_access.roles de Keycloak ("ADMIN") a autoridades de Spring ("ROLE_ADMIN"). */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            Object roles = realmAccess == null ? null : realmAccess.get("roles");
            if (!(roles instanceof Collection<?> lista)) {
                return List.<GrantedAuthority>of();
            }
            return lista.stream()
                    .map(rol -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + rol))
                    .toList();
        });
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                // S11: usa el CorsConfigurationSource de CorsConfig; el preflight OPTIONS pasa sin token
                .cors(cors -> { })
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/actuator/**").permitAll()
                        // Catalogo de prendas: mirar es publico, modificar es ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/api/v1/categorias/**").permitAll()
                        .requestMatchers("/api/v1/productos/**", "/api/v1/categorias/**").hasRole("ADMIN")
                        // Inventario: gestion global
                        .requestMatchers("/api/v1/inventario/**").hasRole("ADMIN")
                        // Clientes: el propio perfil es del CLIENTE, el resto es ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/v1/clientes").hasRole("CLIENTE")
                        .requestMatchers("/api/v1/clientes/me", "/api/v1/clientes/me/**").hasRole("CLIENTE")
                        .requestMatchers("/api/v1/clientes", "/api/v1/clientes/**").hasRole("ADMIN")
                        // Ordenes y pagos
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes").hasRole("ADMIN")
                        .requestMatchers("/api/v1/ordenes/**").hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/pagos/procesar").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/pagos").hasRole("ADMIN")
                        .requestMatchers("/api/v1/pagos/**").hasAnyRole("CLIENTE", "ADMIN")
                        // Todo lo demas exige al menos un JWT valido
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );
        return http.build();
    }
}
