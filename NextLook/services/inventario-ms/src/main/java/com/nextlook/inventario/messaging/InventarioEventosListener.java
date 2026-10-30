package com.nextlook.inventario.messaging;

import com.nextlook.inventario.event.OrdenCreadaEvento;
import com.nextlook.inventario.event.PagoEvento;
import com.nextlook.inventario.event.TipoEvento;
import com.nextlook.inventario.service.ReservaStockService;
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
public class InventarioEventosListener {

    private final ReservaStockService reservaStockService;

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
            reservaStockService.liberarReservas(evento.ordenId(), TipoEvento.ORDEN_CANCELADA);
            return;
        }
        if (!TipoEvento.ORDEN_CREADA.equals(evento.tipoEvento())) {
            return;
        }
        try {
            reservaStockService.reservarParaOrden(evento);
        } catch (DataIntegrityViolationException duplicado) {
            // Capa 2: otra entrega del mismo evento gano la carrera e inserto orden_procesada primero.
            // La transaccion se revirtio entera: no se toco stock ni se publico nada.
            log.info("component=inventario eventType=orden.creada ordenId={} status=ignored motivo=duplicado_concurrente",
                    evento.ordenId());
        }
    }

    @KafkaListener(topics = "${app.kafka.topic.pago}",
            properties = "spring.json.value.default.type=com.nextlook.inventario.event.PagoEvento")
    public void onPago(ConsumerRecord<String, PagoEvento> registro) {
        try (MDC.MDCCloseable trace = TraceKafka.abrir(registro)) {
            procesarPago(registro);
        }
    }

    private void procesarPago(ConsumerRecord<String, PagoEvento> registro) {
        PagoEvento evento = registro.value();
        log.info("component=consumer topic={} partition={} offset={} eventType={} ordenId={} status=consumed",
                registro.topic(), registro.partition(), registro.offset(), evento.tipoEvento(), evento.ordenId());
        switch (evento.tipoEvento()) {
            case TipoEvento.PAGO_APROBADO -> reservaStockService.confirmarReservas(evento.ordenId());
            case TipoEvento.PAGO_RECHAZADO -> reservaStockService.liberarReservas(evento.ordenId(), TipoEvento.PAGO_RECHAZADO);
            default -> log.warn("component=consumer eventType={} status=ignored motivo=tipo_desconocido", evento.tipoEvento());
        }
    }
}
