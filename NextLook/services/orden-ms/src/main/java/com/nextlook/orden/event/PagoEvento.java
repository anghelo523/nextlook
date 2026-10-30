package com.nextlook.orden.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Topic pago-eventos (pago.aprobado | pago.rechazado), lo publica pago-ms. metodoPago = el elegido al pagar. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PagoEvento(String tipoEvento, Long ordenId, Long pagoId, BigDecimal monto, String metodoPago,
                         String idTransaccion, String motivo, String origen, String timestamp) { }
