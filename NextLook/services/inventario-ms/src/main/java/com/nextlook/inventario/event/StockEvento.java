package com.nextlook.inventario.event;

import java.time.Instant;

/** Topic inventario-eventos. tipoEvento = stock.reservado | stock.rechazado */
public record StockEvento(String tipoEvento, Long ordenId, String motivo, String origen, String timestamp) {

    public static StockEvento reservado(Long ordenId) {
        return new StockEvento(TipoEvento.STOCK_RESERVADO, ordenId, null, "inventario-ms", Instant.now().toString());
    }

    public static StockEvento rechazado(Long ordenId, String motivo) {
        return new StockEvento(TipoEvento.STOCK_RECHAZADO, ordenId, motivo, "inventario-ms", Instant.now().toString());
    }
}
