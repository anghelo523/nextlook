package com.nextlook.inventario.dto;

import java.time.LocalDateTime;

public record StockResponse(Long varianteId, String sku, Integer disponible, Integer reservado,
                            LocalDateTime fechaActualizacion) { }
