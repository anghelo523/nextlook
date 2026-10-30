package com.nextlook.pago.messaging;

import com.nextlook.pago.event.OrdenCreadaEvento;
import com.nextlook.pago.event.StockEvento;
import com.nextlook.pago.event.TipoEvento;
import com.nextlook.pago.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventosListener {

    private final PagoService pagoService;

    @KafkaListener(topics = "${app.kafka.topic.orden}")
    public void onOrden(ConsumerRecord<String, OrdenCreadaEvento> registro) {
        try (MDC.MDCCloseable trace = TraceKafka.abrir(registro)) {
            procesarOrden(registro);
        }
    }

    private void procesarOrden(ConsumerRecord<String, OrdenCreadaEvento> registro) {
        OrdenCreadaEvento evento = registro.value();
        log.info("component=consumer topic={} partition={} offset={} eventType={} ordenId={} status=consumed",
                registro.topic(), registro.partition(), registro.offset(), evento.tipoEvento(), evento.ordenId());
        if (TipoEvento.ORDEN_CANCELADA.equals(evento.tipoEvento())) {
            pagoService.anularPorCancelacion(evento.ordenId());
            return;
        }
        if (!TipoEvento.ORDEN_CREADA.equals(evento.tipoEvento())) {
            return;
        }
        try {
            pagoService.registrarPendiente(evento);
        } catch (DataIntegrityViolationException duplicado) {
            log.info("component=pago eventType=orden.creada ordenId={} status=ignored motivo=duplicado_concurrente",
                    evento.ordenId());
        }
    }

    @KafkaListener(topics = "${app.kafka.topic.inventario}",
            properties = "spring.json.value.default.type=com.nextlook.pago.event.StockEvento")
    public void onInventario(ConsumerRecord<String, StockEvento> registro) {
        try (MDC.MDCCloseable trace = TraceKafka.abrir(registro)) {
            procesarInventario(registro);
        }
    }

    private void procesarInventario(ConsumerRecord<String, StockEvento> registro) {
        StockEvento evento = registro.value();
        log.info("component=consumer topic={} partition={} offset={} eventType={} ordenId={} status=consumed",
                registro.topic(), registro.partition(), registro.offset(), evento.tipoEvento(), evento.ordenId());
        switch (evento.tipoEvento()) {
            case TipoEvento.STOCK_RESERVADO -> pagoService.habilitarCobro(evento.ordenId());
            case TipoEvento.STOCK_RECHAZADO -> pagoService.anular(evento.ordenId(), evento.motivo());
            default -> log.warn("component=consumer eventType={} status=ignored motivo=tipo_desconocido", evento.tipoEvento());
        }
    }
}
