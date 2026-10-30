package com.nextlook.inventario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orden_procesada")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrdenProcesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orden_id", nullable = false, unique = true)
    private Long ordenId;

    /** RESERVADO o RECHAZADO */
    @Column(nullable = false, length = 20)
    private String resultado;

    @Column(length = 300)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    void prePersist() {
        fecha = LocalDateTime.now();
    }
}
