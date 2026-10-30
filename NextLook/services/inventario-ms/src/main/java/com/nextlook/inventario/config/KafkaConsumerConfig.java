package com.nextlook.inventario.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Manejo de errores del consumidor.
 * - Un mensaje que no se puede deserializar (DeserializationException) NO se reintenta:
 *   se registra y se salta, para no bloquear la particion (S08).
 * - Cualquier otra excepcion se reintenta 5 veces cada 2 s antes de registrarse y saltarse.
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                (registro, ex) -> log.error(
                        "component=consumer topic={} partition={} offset={} key={} status=skipped error=\"{}\"",
                        registro.topic(), registro.partition(), registro.offset(), registro.key(),
                        ex.getMessage()),
                new FixedBackOff(2000L, 5L));
        return handler;
    }
}
