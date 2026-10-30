package com.nextlook.pago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * titular: APRO aprueba, FUND u OTHE rechaza (pasarela simulada).
 * metodoPago: opcional; el comprador elige TARJETA, YAPE o PLIN al momento de pagar.
 * Si no viene, se cobra con el metodo que marco al crear la orden.
 */
public record ProcesarPagoRequest(
        @NotNull Long ordenId,
        @NotBlank @Size(max = 80) String titular,
        @Pattern(regexp = "TARJETA|YAPE|PLIN", message = "debe ser TARJETA, YAPE o PLIN") String metodoPago
) { }
