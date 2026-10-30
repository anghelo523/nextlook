package com.nextlook.pago.service;

import com.nextlook.pago.dto.PagoResponse;
import com.nextlook.pago.dto.ProcesarPagoRequest;
import com.nextlook.pago.entity.EstadoPago;
import com.nextlook.pago.entity.Pago;
import com.nextlook.pago.event.OrdenCreadaEvento;
import com.nextlook.pago.event.PagoEvento;
import com.nextlook.pago.event.TipoEvento;
import com.nextlook.pago.exception.AccesoDenegadoException;
import com.nextlook.pago.exception.PagoAunNoRegistradoException;
import com.nextlook.pago.exception.ReglaNegocioException;
import com.nextlook.pago.exception.ResourceNotFoundException;
import com.nextlook.pago.gateway.PasarelaPago;
import com.nextlook.pago.gateway.ResultadoPasarela;
import com.nextlook.pago.messaging.EventoPublisher;
import com.nextlook.pago.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PasarelaPago pasarela;
    private final EventoPublisher eventoPublisher;

    @Value("${app.kafka.topic.pago}")
    private String topicPago;

    // ---------------- Reacciones a eventos (S08 / S09) ----------------

    /** Capa 1 de idempotencia: findByOrdenId. Capa 2: UNIQUE(orden_id), atrapado en el listener. */
    @Transactional
    public void registrarPendiente(OrdenCreadaEvento evento) {
        if (pagoRepository.findByOrdenId(evento.ordenId()).isPresent()) {
            log.info("component=pago eventType=orden.creada ordenId={} status=ignored motivo=ya_registrado", evento.ordenId());
            return;
        }
        pagoRepository.saveAndFlush(Pago.builder()
                .ordenId(evento.ordenId())
                .keycloakId(evento.keycloakId())
                .idCliente(evento.idCliente())
                .monto(evento.total())
                .metodoPago(evento.metodoPago())
                .estado(EstadoPago.PENDIENTE)
                .build());
        log.info("component=pago eventType=orden.creada ordenId={} monto={} status=processed estado=PENDIENTE",
                evento.ordenId(), evento.total());
    }

    @Transactional
    public void habilitarCobro(Long ordenId) {
        Pago pago = pagoRepository.findByOrdenId(ordenId).orElseThrow(() -> new PagoAunNoRegistradoException(ordenId));
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            log.info("component=pago eventType=stock.reservado ordenId={} estadoActual={} status=ignored", ordenId, pago.getEstado());
            return;
        }
        pago.setEstado(EstadoPago.LISTO_PARA_PAGAR);
        log.info("component=pago eventType=stock.reservado ordenId={} status=processed estado=LISTO_PARA_PAGAR", ordenId);
    }

    @Transactional
    public void anular(Long ordenId, String motivo) {
        Pago pago = pagoRepository.findByOrdenId(ordenId).orElseThrow(() -> new PagoAunNoRegistradoException(ordenId));
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            log.info("component=pago eventType=stock.rechazado ordenId={} estadoActual={} status=ignored", ordenId, pago.getEstado());
            return;
        }
        pago.setEstado(EstadoPago.ANULADO);
        pago.setMotivo("Sin stock: " + motivo);
        log.info("component=pago eventType=stock.rechazado ordenId={} status=compensated estado=ANULADO", ordenId);
    }

    /**
     * orden.cancelada: el comprador desistio. Si el cobro aun no se hizo, se anula y ya no se puede pagar.
     * Si ya habia resultado de la pasarela (APROBADO/RECHAZADO), no se toca: la orden ya habia decidido.
     */
    @Transactional
    public void anularPorCancelacion(Long ordenId) {
        Pago pago = pagoRepository.bloquearPorOrden(ordenId).orElseThrow(() -> new PagoAunNoRegistradoException(ordenId));
        if (pago.getEstado() != EstadoPago.PENDIENTE && pago.getEstado() != EstadoPago.LISTO_PARA_PAGAR) {
            log.warn("component=pago eventType=orden.cancelada ordenId={} estadoActual={} status=ignored",
                    ordenId, pago.getEstado());
            return;
        }
        pago.setEstado(EstadoPago.ANULADO);
        pago.setMotivo("Cancelado por el cliente");
        log.info("component=pago eventType=orden.cancelada ordenId={} status=compensated estado=ANULADO", ordenId);
    }

    // ---------------- Accion del comprador ----------------

    @Transactional
    public PagoResponse procesar(ProcesarPagoRequest request, String keycloakId) {
        Pago pago = pagoRepository.bloquearPorOrden(request.ordenId())
                .orElseThrow(() -> new ResourceNotFoundException("No hay un pago registrado para la orden " + request.ordenId()));
        if (!pago.getKeycloakId().equals(keycloakId)) {
            throw new AccesoDenegadoException("La orden " + request.ordenId() + " no te pertenece");
        }
        switch (pago.getEstado()) {
            case PENDIENTE -> throw new ReglaNegocioException("Todavia estamos separando tus prendas; intenta en unos segundos");
            case APROBADO -> throw new ReglaNegocioException("Esta orden ya fue pagada");
            case RECHAZADO, ANULADO -> throw new ReglaNegocioException("La orden ya no se puede pagar (estado " + pago.getEstado() + ")");
            case LISTO_PARA_PAGAR -> { }
        }
        if (request.metodoPago() != null) {
            pago.setMetodoPago(request.metodoPago());
        }

        ResultadoPasarela resultado = pasarela.cobrar(pago.getOrdenId(), pago.getMonto(), pago.getMetodoPago(), request.titular());
        pago.setTitular(request.titular());
        pago.setIdTransaccion(resultado.idTransaccion());
        pago.setMotivo(resultado.motivo());
        pago.setEstado(resultado.aprobado() ? EstadoPago.APROBADO : EstadoPago.RECHAZADO);

        String tipo = resultado.aprobado() ? TipoEvento.PAGO_APROBADO : TipoEvento.PAGO_RECHAZADO;
        eventoPublisher.publicarTrasCommit(topicPago, pago.getOrdenId(), tipo,
                PagoEvento.de(tipo, pago.getOrdenId(), pago.getId(), pago.getMonto(), pago.getMetodoPago(),
                        resultado.idTransaccion(), resultado.motivo()));
        log.info("component=pago action=procesar ordenId={} estado={} idTransaccion={}",
                pago.getOrdenId(), pago.getEstado(), resultado.idTransaccion());
        return toResponse(pago);
    }

    @Transactional(readOnly = true)
    public PagoResponse deOrden(Long ordenId, String keycloakId, boolean esAdmin) {
        Pago pago = pagoRepository.findByOrdenId(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay un pago registrado para la orden " + ordenId));
        if (!esAdmin && !pago.getKeycloakId().equals(keycloakId)) {
            throw new AccesoDenegadoException("La orden " + ordenId + " no te pertenece");
        }
        return toResponse(pago);
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoRepository.findAllByOrderByIdDesc().stream().map(this::toResponse).toList();
    }

    private PagoResponse toResponse(Pago p) {
        return new PagoResponse(p.getId(), p.getOrdenId(), p.getIdCliente(), p.getMonto(), p.getMetodoPago(),
                p.getEstado().name(), p.getIdTransaccion(), p.getMotivo(), p.getFechaCreacion(), p.getFechaActualizacion());
    }
}
