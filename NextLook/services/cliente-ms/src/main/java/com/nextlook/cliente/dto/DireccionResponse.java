package com.nextlook.cliente.dto;

public record DireccionResponse(Long id, String alias, String direccion, String distrito,
                                String ciudad, String referencia, Boolean principal) {

    /** Texto con el que orden-ms guarda la direccion de envio como copia historica. */
    public String textoCompleto() {
        return direccion + ", " + distrito + ", " + ciudad + (referencia == null ? "" : " (" + referencia + ")");
    }
}
