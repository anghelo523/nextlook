package com.nextlook.inventario.dto;

import java.time.LocalDateTime;

public record ReservaResponse(Long id, Long ordenId, Long varianteId, Integer cantidad, String estado,
                              LocalDateTime fechaActualizacion) { }
