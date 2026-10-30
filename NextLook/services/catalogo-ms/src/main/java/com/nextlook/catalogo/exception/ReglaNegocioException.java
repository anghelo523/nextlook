package com.nextlook.catalogo.exception;

/** Violacion de una regla de negocio: se responde 409 Conflict. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
