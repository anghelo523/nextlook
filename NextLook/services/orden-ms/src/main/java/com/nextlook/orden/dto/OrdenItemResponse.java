package com.nextlook.orden.dto;

import java.math.BigDecimal;

public record OrdenItemResponse(Long varianteId, String nombrePrenda, String talla, String color,
                                BigDecimal precioUnitario, Integer cantidad, BigDecimal subtotal) { }
