package com.nextlook.pago.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Topic pago-eventos. tipoEvento = pago.aprobado | pago.rechazado
 * metodoPago: el que el comprador eligio al pagar (puede no ser el que marco al crear la orden).
 */
public record PagoEvento(String tipoEvento, Long ordenId, Long pagoId, BigDecimal monto, String metodoPago,
                         String idTransaccion, String motivo, String origen, String timestamp) {

    public static PagoEvento de(String tipo, Long ordenId, Long pagoId, BigDecimal monto, String metodoPago,
                                String idTransaccion, String motivo) {
        return new PagoEvento(tipo, ordenId, pagoId, monto, metodoPago, idTransaccion, motivo, "pago-ms",
                Instant.now().toString());
    }
}
