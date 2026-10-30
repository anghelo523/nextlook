package com.nextlook.orden.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Topic inventario-eventos (stock.reservado | stock.rechazado), lo publica inventario-ms. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StockEvento(String tipoEvento, Long ordenId, String motivo, String origen, String timestamp) { }
