package com.nextlook.orden.event;

import java.time.Instant;

/**
 * Topic orden-eventos, key = ordenId (misma particion que su orden.creada, asi llega despues).
 * Lo consumen inventario-ms (libera las prendas separadas) y pago-ms (anula el cobro pendiente).
 */
public record OrdenCanceladaEvento(String tipoEvento, Long ordenId, String motivo, String origen, String timestamp) {

    public static OrdenCanceladaEvento de(Long ordenId, String motivo) {
        return new OrdenCanceladaEvento(TipoEvento.ORDEN_CANCELADA, ordenId, motivo, "orden-ms", Instant.now().toString());
    }
}
