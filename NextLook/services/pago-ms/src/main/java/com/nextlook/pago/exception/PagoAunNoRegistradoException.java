package com.nextlook.pago.exception;

/** stock.* llego antes que orden.creada (son topics distintos). Se reintenta con el DefaultErrorHandler. */
public class PagoAunNoRegistradoException extends RuntimeException {
    public PagoAunNoRegistradoException(Long ordenId) {
        super("Aun no existe el pago de la orden " + ordenId + "; se reintentara");
    }
}
