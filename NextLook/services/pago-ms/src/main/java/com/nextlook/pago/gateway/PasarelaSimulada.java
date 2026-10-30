package com.nextlook.pago.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

/**
 * Pasarela SIMULADA para la demo. Usa la misma convencion que las tarjetas de prueba de Mercado Pago:
 * el nombre del titular decide el resultado.
 *   APRO -> aprobado
 *   FUND -> rechazado por fondos insuficientes
 *   OTHE (o cualquier otro) -> rechazado
 */
@Slf4j
@Component
public class PasarelaSimulada implements PasarelaPago {

    @Override
    public ResultadoPasarela cobrar(Long ordenId, BigDecimal monto, String metodoPago, String titular) {
        String clave = titular == null ? "" : titular.trim().toUpperCase(Locale.ROOT);
        String idTransaccion = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        ResultadoPasarela resultado = switch (clave) {
            case "APRO" -> new ResultadoPasarela(true, idTransaccion, null);
            case "FUND" -> new ResultadoPasarela(false, idTransaccion, "Fondos insuficientes");
            default -> new ResultadoPasarela(false, idTransaccion, "Pago rechazado por la entidad emisora");
        };
        log.info("component=pasarela ordenId={} monto={} metodo={} aprobado={} idTransaccion={}",
                ordenId, monto, metodoPago, resultado.aprobado(), idTransaccion);
        return resultado;
    }
}
