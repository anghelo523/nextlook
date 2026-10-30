package com.nextlook.orden.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, length = 36)
    private String keycloakId;

    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "nombre_cliente", length = 170)
    private String nombreCliente;

    @Column(name = "direccion_envio", length = 500)
    private String direccionEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 300)
    private String observacion;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<OrdenItem> items = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public void agregarItem(OrdenItem item) {
        item.setOrden(this);
        items.add(item);
    }

    public void recalcularTotal() {
        total = items.stream()
                .filter(i -> i.getPrecioUnitario() != null)
                .map(OrdenItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PrePersist
    void prePersist() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
