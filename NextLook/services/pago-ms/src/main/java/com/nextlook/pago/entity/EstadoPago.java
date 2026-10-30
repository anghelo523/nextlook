package com.nextlook.pago.entity;

/**
 * PENDIENTE         -> se registro al consumir orden.creada; aun no se sabe si hay stock.
 * LISTO_PARA_PAGAR  -> llego stock.reservado; el comprador ya puede pagar.
 * APROBADO / RECHAZADO -> respuesta de la pasarela.
 * ANULADO           -> llego stock.rechazado: no hay nada que cobrar.
 */
public enum EstadoPago { PENDIENTE, LISTO_PARA_PAGAR, APROBADO, RECHAZADO, ANULADO }
