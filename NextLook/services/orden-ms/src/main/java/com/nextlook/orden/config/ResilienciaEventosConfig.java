package com.nextlook.orden.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * U2 / S06 - Deja en el log (y por lo tanto en Loki, S10) cada reintento y cada cambio de estado
 * del Circuit Breaker, con el mismo formato component=... que el resto del sistema.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResilienciaEventosConfig {

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    void registrarEventos() {
        for (String nombre : List.of("cliente", "catalogo")) {
            retryRegistry.retry(nombre).getEventPublisher()
                    .onRetry(e -> log.warn("component=resiliencia target={} status=retry intento={} espera={}ms error=\"{}\"",
                            nombre, e.getNumberOfRetryAttempts(), e.getWaitInterval().toMillis(),
                            e.getLastThrowable() == null ? "" : e.getLastThrowable().toString()))
                    .onError(e -> log.warn("component=resiliencia target={} status=retry_agotado intentos={}",
                            nombre, e.getNumberOfRetryAttempts()));
            circuitBreakerRegistry.circuitBreaker(nombre).getEventPublisher()
                    .onStateTransition(e -> log.warn("component=resiliencia target={} status=circuito transicion={}",
                            nombre, e.getStateTransition()));
        }
    }
}
