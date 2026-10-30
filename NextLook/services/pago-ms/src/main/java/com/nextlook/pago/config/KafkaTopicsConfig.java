package com.nextlook.pago.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Declara (con 3 particiones) los topics que este servicio publica o consume. */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic ordenEventosTopic(@Value("${app.kafka.topic.orden}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic inventarioEventosTopic(@Value("${app.kafka.topic.inventario}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic pagoEventosTopic(@Value("${app.kafka.topic.pago}") String nombre) {
        return TopicBuilder.name(nombre).partitions(3).replicas(1).build();
    }
}
