package com.nextlook.orden.client;

import com.nextlook.orden.exception.ReglaNegocioException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * S06 - Bean SEPARADO de OrdenService: @CircuitBreaker funciona por proxy, y una llamada
 * interna (this.metodo()) no pasaria por el proxy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteGateway {

    private final ClienteClient clienteClient;

    // U2 - Retry (externo) envuelve al CircuitBreaker (interno): cada intento pasa por el circuito;
    // con el circuito abierto (CallNotPermittedException) no se reintenta y se va directo al fallback.
    @Retry(name = "cliente", fallbackMethod = "perfilNoDisponible")
    @CircuitBreaker(name = "cliente")
    public Optional<ClienteRemoto> obtenerMiPerfil() {
        try {
            return Optional.of(clienteClient.obtenerMiPerfil());
        } catch (FeignException.NotFound e) {
            // Error de negocio, no de infraestructura: esta en ignore-exceptions y no abre el circuito.
            throw new ReglaNegocioException("Antes de comprar registra tu perfil (POST /api/v1/clientes)");
        }
    }

    public Optional<ClienteRemoto> perfilNoDisponible(Throwable ex) {
        if (ex instanceof ReglaNegocioException regla) {
            throw regla;
        }
        log.warn("component=feign target=cliente-ms status=fallback error=\"{}\"", ex.toString());
        return Optional.empty();
    }
}
