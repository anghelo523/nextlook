package com.nextlook.orden.client;

import com.nextlook.orden.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogoGateway {

    private final CatalogoClient catalogoClient;

    // U2 - Retry (externo) envuelve al CircuitBreaker (interno): cada intento pasa por el circuito;
    // con el circuito abierto (CallNotPermittedException) no se reintenta y se va directo al fallback.
    @Retry(name = "catalogo", fallbackMethod = "catalogoNoDisponible")
    @CircuitBreaker(name = "catalogo")
    public Optional<VarianteRemota> obtenerVariante(Long varianteId) {
        try {
            return Optional.of(catalogoClient.obtenerVariante(varianteId));
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("La variante " + varianteId + " no existe en el catalogo");
        }
    }

    public Optional<VarianteRemota> catalogoNoDisponible(Long varianteId, Throwable ex) {
        if (ex instanceof ResourceNotFoundException noExiste) {
            throw noExiste;
        }
        log.warn("component=feign target=catalogo-ms varianteId={} status=fallback error=\"{}\"",
                varianteId, ex.toString());
        return Optional.empty();
    }
}
