package com.nextlook.orden.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** Respuesta de catalogo-ms GET /api/v1/productos/variantes/{id}: una prenda en una talla y color. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VarianteRemota(Long varianteId, Long prendaId, String nombrePrenda, String talla, String color,
                             String sku, BigDecimal precio, Boolean disponibleParaVenta) { }
