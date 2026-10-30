package com.nextlook.catalogo.dto;

import com.nextlook.catalogo.entity.Genero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PrendaRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 255) String descripcion,
        @Size(max = 80) String marca,
        @NotNull Genero genero,
        @NotNull @DecimalMin(value = "0.10") BigDecimal precio,
        @NotNull Long categoriaId,
        @Valid List<VarianteRequest> variantes
) { }
