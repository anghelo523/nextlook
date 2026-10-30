package com.nextlook.orden.dto;

import com.nextlook.orden.entity.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** El comprador solo envia que prendas (variante = talla + color) y cuantas; el precio lo pone catalogo-ms. */
public record CrearOrdenRequest(
        @NotEmpty @Size(max = 20) List<@Valid ItemRequest> items,
        @NotNull MetodoPago metodoPago
) { }
