package com.nextlook.inventario.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Copia local del contrato que publica orden-ms (solo los campos que inventario necesita). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrdenCreadaEvento(String tipoEvento, Long ordenId, List<Item> items, String origen, String timestamp) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(Long varianteId, Integer cantidad) { }
}
