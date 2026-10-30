package com.nextlook.cliente.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ClienteResponse(
        Long id,
        String nombres,
        String apellidos,
        String email,
        String dni,
        String telefono,
        LocalDateTime fechaRegistro,
        List<DireccionResponse> direcciones,
        DireccionResponse direccionPrincipal
) { }
