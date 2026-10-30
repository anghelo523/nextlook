package com.nextlook.orden.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Lo que orden-ms necesita de la respuesta de cliente-ms (GET /api/v1/clientes/me). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClienteRemoto(Long id, String nombres, String apellidos, String email, DireccionRemota direccionPrincipal) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DireccionRemota(Long id, String alias, String direccion, String distrito,
                                  String ciudad, String referencia) {
        public String texto() {
            return alias + ": " + direccion + ", " + distrito + ", " + ciudad
                    + (referencia == null || referencia.isBlank() ? "" : " (" + referencia + ")");
        }
    }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
