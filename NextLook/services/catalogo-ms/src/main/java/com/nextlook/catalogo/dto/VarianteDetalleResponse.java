package com.nextlook.catalogo.dto;

import java.math.BigDecimal;

/**
 * Lo que orden-ms necesita de una variante para armar la linea de una orden (S06):
 * nombre de la prenda, talla, color y el precio vigente. Todo en una sola consulta.
 */
public record VarianteDetalleResponse(
        Long varianteId,
        Long prendaId,
        String nombrePrenda,
        String talla,
        String color,
        String sku,
        BigDecimal precio,
        Boolean disponibleParaVenta
) { }
