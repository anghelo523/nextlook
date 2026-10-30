package com.nextlook.orden.service;

import com.nextlook.orden.client.CatalogoGateway;
import com.nextlook.orden.client.ClienteGateway;
import com.nextlook.orden.client.ClienteRemoto;
import com.nextlook.orden.client.VarianteRemota;
import com.nextlook.orden.dto.*;
import com.nextlook.orden.entity.EstadoOrden;
import com.nextlook.orden.entity.Orden;
import com.nextlook.orden.entity.OrdenItem;
import com.nextlook.orden.event.OrdenCanceladaEvento;
import com.nextlook.orden.event.OrdenCreadaEvento;
import com.nextlook.orden.event.TipoEvento;
import com.nextlook.orden.exception.AccesoDenegadoException;
import com.nextlook.orden.exception.ReglaNegocioException;
import com.nextlook.orden.exception.ResourceNotFoundException;
import com.nextlook.orden.messaging.EventoPublisher;
import com.nextlook.orden.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ClienteGateway clienteGateway;
    private final CatalogoGateway catalogoGateway;
    private final EventoPublisher eventoPublisher;

    @Value("${app.kafka.topic.orden}")
    private String topicOrden;

    @Transactional
    public OrdenResponse crear(CrearOrdenRequest request, String keycloakId) {
        Orden orden = Orden.builder()
                .keycloakId(keycloakId)
                .metodoPago(request.metodoPago())
                .estado(EstadoOrden.CARRITO)
                .build();
        // Si mandan la misma variante dos veces, se suma la cantidad (UNIQUE id_orden + variante_id).
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        request.items().forEach(i -> cantidades.merge(i.varianteId(), i.cantidad(), Integer::sum));
        cantidades.forEach((varianteId, cantidad) -> {
            if (cantidad > 10) {
                throw new ReglaNegocioException("Maximo 10 unidades por prenda (variante " + varianteId + ")");
            }
            orden.agregarItem(OrdenItem.builder().varianteId(varianteId).cantidad(cantidad).build());
        });

        validarContraServicios(orden);
        Orden guardada = ordenRepository.save(orden);
        publicarSiQuedoValidada(guardada);
        return toResponse(guardada);
    }

    /** Reintento manual desde el carrito cuando cliente-ms o catalogo-ms ya se recuperaron. */
    @Transactional
    public OrdenResponse confirmarCarrito(Long ordenId, String keycloakId) {
        Orden orden = buscar(ordenId);
        if (!orden.getKeycloakId().equals(keycloakId)) {
            throw new AccesoDenegadoException("La orden " + ordenId + " no te pertenece");
        }
        if (orden.getEstado() != EstadoOrden.CARRITO) {
            throw new ReglaNegocioException("Solo se confirma una orden en CARRITO; estado actual: " + orden.getEstado());
        }
        validarContraServicios(orden);
        publicarSiQuedoValidada(orden);
        return toResponse(orden);
    }

    /**
     * El comprador ya no quiere la orden. Desde CARRITO no se habia publicado nada: basta con cancelarla.
     * Desde PENDIENTE_PAGO las prendas estan separadas y el cobro registrado: se publica orden.cancelada
     * para que inventario-ms las libere y pago-ms anule el cobro (misma compensacion que un pago rechazado).
     */
    @Transactional
    public OrdenResponse cancelar(Long ordenId, String keycloakId) {
        Orden orden = buscar(ordenId);
        if (!orden.getKeycloakId().equals(keycloakId)) {
            throw new AccesoDenegadoException("La orden " + ordenId + " no te pertenece");
        }
        EstadoOrden anterior = orden.getEstado();
        switch (anterior) {
            case CARRITO, PENDIENTE_PAGO -> { }
            case PENDIENTE_STOCK -> throw new ReglaNegocioException(
                    "Todavia estamos separando tus prendas; podras cancelar en unos segundos");
            case PAGADA -> throw new ReglaNegocioException("La orden ya fue pagada: no se puede cancelar");
            case CANCELADA -> throw new ReglaNegocioException("La orden ya esta cancelada");
        }
        orden.setEstado(EstadoOrden.CANCELADA);
        orden.setObservacion("CANCELADA_POR_CLIENTE: el comprador desistio de la compra");
        if (anterior == EstadoOrden.PENDIENTE_PAGO) {
            eventoPublisher.publicarTrasCommit(topicOrden, orden.getId(), TipoEvento.ORDEN_CANCELADA,
                    OrdenCanceladaEvento.de(orden.getId(), "Cancelada por el cliente"));
        }
        log.info("component=orden action=cancelar ordenId={} estadoAnterior={} status=cancelled", ordenId, anterior);
        return toResponse(orden);
    }

    /**
     * S06 - Llamadas sincronas protegidas. Si alguna dependencia no responde (fallback),
     * la orden NO se pierde: queda en CARRITO y el comprador puede confirmarla luego.
     */
    private void validarContraServicios(Orden orden) {
        Optional<ClienteRemoto> cliente = clienteGateway.obtenerMiPerfil();
        if (cliente.isEmpty()) {
            dejarEnCarrito(orden, "cliente-ms no disponible");
            return;
        }
        if (cliente.get().direccionPrincipal() == null) {
            throw new ReglaNegocioException("Agrega una direccion de envio (POST /api/v1/clientes/me/direcciones)");
        }
        for (OrdenItem item : orden.getItems()) {
            Optional<VarianteRemota> variante = catalogoGateway.obtenerVariante(item.getVarianteId());
            if (variante.isEmpty()) {
                dejarEnCarrito(orden, "catalogo-ms no disponible");
                return;
            }
            VarianteRemota v = variante.get();
            if (!Boolean.TRUE.equals(v.disponibleParaVenta())) {
                throw new ReglaNegocioException(v.nombrePrenda() + " talla " + v.talla() + " " + v.color()
                        + " ya no esta a la venta");
            }
            item.setPrendaId(v.prendaId());
            item.setNombrePrenda(v.nombrePrenda());
            item.setTalla(v.talla());
            item.setColor(v.color());
            item.setPrecioUnitario(v.precio());
        }
        orden.setIdCliente(cliente.get().id());
        orden.setNombreCliente(cliente.get().nombreCompleto());
        orden.setDireccionEnvio(cliente.get().direccionPrincipal().texto());
        orden.recalcularTotal();
        orden.setEstado(EstadoOrden.PENDIENTE_STOCK);
        orden.setObservacion("Esperando confirmacion de stock");
    }

    private void dejarEnCarrito(Orden orden, String causa) {
        orden.setEstado(EstadoOrden.CARRITO);
        orden.setObservacion(causa + ": tu pedido quedo guardado en el carrito, confirmalo en unos minutos");
        log.warn("component=orden action=crear status=carrito causa=\"{}\"", causa);
    }

    /** S08 - Solo una orden validada genera el evento, y solo despues del commit. */
    private void publicarSiQuedoValidada(Orden orden) {
        if (orden.getEstado() != EstadoOrden.PENDIENTE_STOCK) {
            return;
        }
        List<OrdenCreadaEvento.Item> items = orden.getItems().stream()
                .map(i -> new OrdenCreadaEvento.Item(i.getVarianteId(), i.getCantidad()))
                .toList();
        OrdenCreadaEvento evento = OrdenCreadaEvento.de(orden.getId(), orden.getIdCliente(), orden.getKeycloakId(),
                orden.getTotal(), orden.getMetodoPago().name(), items);
        eventoPublisher.publicarTrasCommit(topicOrden, orden.getId(), TipoEvento.ORDEN_CREADA, evento);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> misOrdenes(String keycloakId) {
        return ordenRepository.findByKeycloakIdOrderByIdDesc(keycloakId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtener(Long id, String keycloakId, boolean esAdmin) {
        Orden orden = buscar(id);
        if (!esAdmin && !orden.getKeycloakId().equals(keycloakId)) {
            throw new AccesoDenegadoException("La orden " + id + " no te pertenece");
        }
        return toResponse(orden);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarTodas() {
        return ordenRepository.findAllByOrderByIdDesc().stream().map(this::toResponse).toList();
    }

    private Orden buscar(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));
    }

    private OrdenResponse toResponse(Orden o) {
        List<OrdenItemResponse> items = o.getItems().stream()
                .map(i -> new OrdenItemResponse(i.getVarianteId(), i.getNombrePrenda(), i.getTalla(), i.getColor(),
                        i.getPrecioUnitario(), i.getCantidad(), i.subtotal()))
                .toList();
        return new OrdenResponse(o.getId(), o.getIdCliente(), o.getNombreCliente(), o.getDireccionEnvio(),
                o.getEstado().name(), o.getMetodoPago().name(), o.getTotal(), o.getObservacion(), items,
                o.getFechaCreacion(), o.getFechaActualizacion());
    }
}
