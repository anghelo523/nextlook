package com.nextlook.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Lo que el CLIENTE completa de su perfil. El keycloakId y el email salen del JWT, no de aqui. */
public record PerfilRequest(
        @NotBlank @Size(max = 80) String nombres,
        @NotBlank @Size(max = 80) String apellidos,
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos") String dni,
        @Pattern(regexp = "9\\d{8}", message = "El celular debe tener 9 digitos") String telefono
) { }
