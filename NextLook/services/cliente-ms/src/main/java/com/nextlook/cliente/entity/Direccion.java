package com.nextlook.cliente.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "direcciones")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, length = 40)
    private String alias;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 80)
    private String distrito;

    @Column(nullable = false, length = 80)
    private String ciudad;

    @Column(length = 200)
    private String referencia;

    @Column(nullable = false)
    @Builder.Default
    private Boolean principal = Boolean.FALSE;
}
