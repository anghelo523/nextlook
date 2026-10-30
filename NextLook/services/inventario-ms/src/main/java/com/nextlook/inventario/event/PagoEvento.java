package com.nextlook.inventario.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PagoEvento(String tipoEvento, Long ordenId, String motivo, String origen, String timestamp) { }
