package com.nextlook.pago.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(Long id, Long ordenId, Long idCliente, BigDecimal monto, String metodoPago,
                           String estado, String idTransaccion, String motivo,
                           LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) { }
