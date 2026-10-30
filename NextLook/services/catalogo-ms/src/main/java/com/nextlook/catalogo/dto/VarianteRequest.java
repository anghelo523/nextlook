package com.nextlook.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VarianteRequest(
        @NotBlank @Size(max = 10) String talla,
        @NotBlank @Size(max = 40) String color,
        @NotBlank @Size(max = 40) String sku
) { }
