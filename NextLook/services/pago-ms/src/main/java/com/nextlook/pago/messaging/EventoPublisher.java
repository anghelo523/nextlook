package com.nextlook.pago.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * S08 - Publica un evento SOLO despues de que la transaccion local se confirmo (afterCommit).
 * Si el commit falla, nadie se entera de un hecho que no ocurrio.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventoPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publicarTrasCommit(String topic, Long ordenId, String tipoEvento, Object evento) {
        // S10: se lee AHORA, en el hilo que atiende la peticion (o el evento) que origina este evento
        String traceId = MDC.get(TraceKafka.MDC_KEY);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(topic, ordenId, tipoEvento, evento, traceId);
                }
            });
        } else {
            enviar(topic, ordenId, tipoEvento, evento, traceId);
        }
    }

    private void enviar(String topic, Long ordenId, String tipoEvento, Object evento, String traceId) {
        // key = ordenId: todos los eventos de una orden van a la misma particion, en orden
        ProducerRecord<String, Object> registro = new ProducerRecord<>(topic, String.valueOf(ordenId), evento);
        if (traceId != null) {
            registro.headers().add(TraceKafka.HEADER, TraceKafka.valor(traceId));
        }
        kafkaTemplate.send(registro)
                .whenComplete((resultado, ex) -> {
                    if (ex != null) {
                        log.error("component=producer topic={} eventType={} ordenId={} status=error error=\"{}\"",
                                topic, tipoEvento, ordenId, ex.getMessage());
                        return;
                    }
                    log.info("component=producer topic={} partition={} offset={} eventType={} ordenId={} traceId={} status=published",
                            resultado.getRecordMetadata().topic(),
                            resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset(),
                            tipoEvento, ordenId, traceId);
                });
    }
}
