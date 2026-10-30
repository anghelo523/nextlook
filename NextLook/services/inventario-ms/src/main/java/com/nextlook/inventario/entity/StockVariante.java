package com.nextlook.inventario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_variante")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StockVariante {

    @Id
    @Column(name = "variante_id")
    private Long varianteId;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    /** Unidades que se pueden vender ahora mismo. */
    @Column(nullable = false)
    private Integer disponible;

    /** Unidades separadas para ordenes que todavia no se pagan. */
    @Column(nullable = false)
    private Integer reservado;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    void tocar() {
        fechaActualizacion = LocalDateTime.now();
    }
}
