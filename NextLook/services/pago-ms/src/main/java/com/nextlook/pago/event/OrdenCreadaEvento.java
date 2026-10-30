package com.nextlook.pago.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Copia local del contrato de orden-ms: pago-ms no necesita los items, solo cuanto y de quien. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdenCreadaEvento(String tipoEvento, Long ordenId, Long idCliente, String keycloakId,
                                BigDecimal total, String metodoPago, String origen, String timestamp) { }
