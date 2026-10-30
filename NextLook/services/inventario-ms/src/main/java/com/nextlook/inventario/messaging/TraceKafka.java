package com.nextlook.inventario.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

/**
 * S10 - El MDC es por hilo: el hilo del @KafkaListener no hereda el traceId del hilo HTTP que
 * publico el evento. Por eso el productor lo copia en un header del mensaje (X-Trace-ID) y el
 * consumidor lo vuelve a poner en SU MDC mientras procesa ese mensaje.
 */
public final class TraceKafka {

    public static final String HEADER = "X-Trace-ID";
    public static final String MDC_KEY = "traceId";

    private TraceKafka() { }

    public static byte[] valor(String traceId) {
        return traceId.getBytes(StandardCharsets.UTF_8);
    }

    /** Uso: try (MDC.MDCCloseable t = TraceKafka.abrir(registro)) { ... } */
    public static MDC.MDCCloseable abrir(ConsumerRecord<?, ?> registro) {
        Header header = registro.headers().lastHeader(HEADER);
        String traceId = header != null
                ? new String(header.value(), StandardCharsets.UTF_8)
                // evento publicado sin traceId (por ejemplo, reenviado a mano desde Kafka UI)
                : "kafka-" + registro.topic() + "-" + registro.partition() + "-" + registro.offset();
        return MDC.putCloseable(MDC_KEY, traceId);
    }
}
