package com.nextlook.orden.exception;

/**
 * Llego un evento antes que el que deberia precederlo (topics distintos no garantizan orden entre si).
 * Se lanza para que el DefaultErrorHandler lo reintente unos segundos despues.
 */
public class EventoFueraDeSecuenciaException extends RuntimeException {
    public EventoFueraDeSecuenciaException(String message) {
        super(message);
    }
}
