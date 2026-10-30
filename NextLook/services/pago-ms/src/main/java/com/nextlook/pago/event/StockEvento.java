package com.nextlook.pago.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockEvento(String tipoEvento, Long ordenId, String motivo, String origen, String timestamp) { }
