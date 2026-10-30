package com.nextlook.orden.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Topic orden-eventos, key = ordenId. Lo consumen inventario-ms y pago-ms. */
public record OrdenCreadaEvento(
        String tipoEvento,
        Long ordenId,
        Long idCliente,
        String keycloakId,
        BigDecimal total,
        String metodoPago,
        List<Item> items,
        String origen,
        String timestamp
) {
    public record Item(Long varianteId, Integer cantidad) { }

    public static OrdenCreadaEvento de(Long ordenId, Long idCliente, String keycloakId, BigDecimal total,
                                       String metodoPago, List<Item> items) {
        return new OrdenCreadaEvento(TipoEvento.ORDEN_CREADA, ordenId, idCliente, keycloakId, total, metodoPago,
                items, "orden-ms", Instant.now().toString());
    }
}
