package com.nextlook.orden.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
        Long id,
        Long idCliente,
        String nombreCliente,
        String direccionEnvio,
        String estado,
        String metodoPago,
        BigDecimal total,
        String observacion,
        List<OrdenItemResponse> items,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) { }
