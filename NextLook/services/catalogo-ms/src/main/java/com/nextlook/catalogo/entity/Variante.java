package com.nextlook.catalogo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "variantes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Variante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prenda", nullable = false)
    private Prenda prenda;

    @Column(nullable = false, length = 10)
    private String talla;

    @Column(nullable = false, length = 40)
    private String color;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = Boolean.TRUE;
}
