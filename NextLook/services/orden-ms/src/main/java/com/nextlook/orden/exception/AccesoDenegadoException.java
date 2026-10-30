package com.nextlook.orden.exception;

/** El recurso existe pero no pertenece a quien lo pide: se responde 403. */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
