package com.nextlook.catalogo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Foto de una prenda. Va en su propia tabla para que listar el catalogo no arrastre los bytes. */
@Entity
@Table(name = "prenda_imagenes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PrendaImagen {

    @Id
    @Column(name = "id_prenda")
    private Long prendaId;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false)
    private byte[] contenido;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    void alGuardar() {
        fechaActualizacion = LocalDateTime.now();
    }
}
