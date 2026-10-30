package com.nextlook.orden.messaging;

import com.nextlook.orden.event.PagoEvento;
import com.nextlook.orden.event.StockEvento;
import com.nextlook.orden.event.TipoEvento;
import com.nextlook.orden.service.SagaOrdenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventosListener {

    private final SagaOrdenService sagaOrdenService;

    @KafkaListener(topics = "${app.kafka.topic.inventario}")
    public void onInventario(ConsumerRecord<String, StockEvento> registro) {
        try (MDC.MDCCloseable trace = TraceKafka.abrir(registro)) {
            procesarInventario(registro);
        }
    }

    private void procesarInventario(ConsumerRecord<String, StockEvento> registro) {
        StockEvento evento = registro.value();
        consumido(registro, evento.tipoEvento(), evento.ordenId());
        switch (evento.tipoEvento()) {
            case TipoEvento.STOCK_RESERVADO -> sagaOrdenService.alStockReservado(evento.ordenId());
            case TipoEvento.STOCK_RECHAZADO -> sagaOrdenService.alStockRechazado(evento.ordenId(), evento.motivo());
            default -> log.warn("component=consumer eventType={} status=ignored motivo=tipo_desconocido", evento.tipoEvento());
        }
    }

    // El topic de pagos trae otro contrato: se cambia el tipo por defecto SOLO para este listener.
    @KafkaListener(topics = "${app.kafka.topic.pago}",
            properties = "spring.json.value.default.type=com.nextlook.orden.event.PagoEvento")
    public void onPago(ConsumerRecord<String, PagoEvento> registro) {
        try (MDC.MDCCloseable trace = TraceKafka.abrir(registro)) {
            procesarPago(registro);
        }
    }

    private void procesarPago(ConsumerRecord<String, PagoEvento> registro) {
        PagoEvento evento = registro.value();
        consumido(registro, evento.tipoEvento(), evento.ordenId());
        switch (evento.tipoEvento()) {
            case TipoEvento.PAGO_APROBADO -> sagaOrdenService.alPagoAprobado(evento.ordenId(), evento.metodoPago());
            case TipoEvento.PAGO_RECHAZADO -> sagaOrdenService.alPagoRechazado(evento.ordenId(), evento.motivo(), evento.metodoPago());
            default -> log.warn("component=consumer eventType={} status=ignored motivo=tipo_desconocido", evento.tipoEvento());
        }
    }

    private void consumido(ConsumerRecord<String, ?> registro, String tipo, Long ordenId) {
        log.info("component=consumer topic={} partition={} offset={} eventType={} ordenId={} status=consumed",
                registro.topic(), registro.partition(), registro.offset(), tipo, ordenId);
    }
}
