package com.nextlook.orden.entity;

/**
 * CARRITO          -> no se pudo validar con cliente-ms o catalogo-ms (fallback del Circuit Breaker, S06).
 * PENDIENTE_STOCK  -> orden validada; se publico orden.creada y se espera a inventario-ms (S08).
 * PENDIENTE_PAGO   -> stock reservado; el cliente ya puede pagar.
 * PAGADA           -> pago aprobado: fin feliz de la Saga (S09).
 * CANCELADA        -> compensacion: sin stock, pago rechazado o el comprador desistio (orden.cancelada).
 */
public enum EstadoOrden { CARRITO, PENDIENTE_STOCK, PENDIENTE_PAGO, PAGADA, CANCELADA }
