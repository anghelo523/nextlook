package com.nextlook.catalogo.dto;

import java.math.BigDecimal;
import java.util.List;

public record PrendaResponse(
        Long id,
        String nombre,
        String descripcion,
        String marca,
        String genero,
        BigDecimal precio,
        Boolean activo,
        CategoriaResponse categoria,
        List<VarianteResponse> variantes,
        /** Ruta relativa al Gateway (/api/v1/productos/{id}/imagen?v=...) o null si no tiene foto. */
        String imagenUrl
) { }
