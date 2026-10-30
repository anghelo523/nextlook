package com.nextlook.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** ADMIN registra o ajusta el stock disponible de una variante (por ejemplo, llego mercaderia). */
public record StockRequest(
        @NotBlank @Size(max = 40) String sku,
        @NotNull @Min(0) Integer disponible
) { }
