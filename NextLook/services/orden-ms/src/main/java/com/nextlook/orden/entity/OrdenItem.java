package com.nextlook.orden.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orden_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrdenItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_orden", nullable = false)
    private Orden orden;

    @Column(name = "variante_id", nullable = false)
    private Long varianteId;

    @Column(name = "prenda_id")
    private Long prendaId;

    @Column(name = "nombre_prenda", length = 120)
    private String nombrePrenda;

    @Column(length = 10)
    private String talla;

    @Column(length = 30)
    private String color;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    public BigDecimal subtotal() {
        return precioUnitario == null ? BigDecimal.ZERO : precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
