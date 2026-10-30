package com.nextlook.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DireccionRequest(
        @NotBlank @Size(max = 40) String alias,
        @NotBlank @Size(max = 200) String direccion,
        @NotBlank @Size(max = 80) String distrito,
        @NotBlank @Size(max = 80) String ciudad,
        @Size(max = 200) String referencia,
        Boolean principal
) { }
