package com.nextlook.inventario.event;

public final class TipoEvento {
    public static final String ORDEN_CREADA = "orden.creada";
    public static final String STOCK_RESERVADO = "stock.reservado";
    public static final String STOCK_RECHAZADO = "stock.rechazado";
    public static final String PAGO_APROBADO = "pago.aprobado";
    public static final String PAGO_RECHAZADO = "pago.rechazado";
    /** El comprador desistio de una orden ya separada: se liberan sus prendas y se anula su cobro. */
    public static final String ORDEN_CANCELADA = "orden.cancelada";

    private TipoEvento() { }
}
