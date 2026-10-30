package com.nextlook.orden.service;

import com.nextlook.orden.entity.EstadoOrden;
import com.nextlook.orden.entity.MetodoPago;
import com.nextlook.orden.entity.Orden;
import com.nextlook.orden.exception.EventoFueraDeSecuenciaException;
import com.nextlook.orden.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * S09 - Saga coreografiada de la compra. Cada transicion esta protegida por el estado actual:
 * si el evento llega repetido (Kafka entrega "al menos una vez"), la guarda lo ignora.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrdenService {

    private final OrdenRepository ordenRepository;

    @Transactional
    public void alStockReservado(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId).orElse(null);
        if (orden == null || orden.getEstado() != EstadoOrden.PENDIENTE_STOCK) {
            ignorado("stock.reservado", ordenId, orden);
            return;
        }
        orden.setEstado(EstadoOrden.PENDIENTE_PAGO);
        orden.setObservacion("Prendas separadas en almacen. Ya puedes pagar");
        log.info("component=saga eventType=stock.reservado ordenId={} estado=PENDIENTE_PAGO status=processed", ordenId);
    }

    @Transactional
    public void alStockRechazado(Long ordenId, String motivo) {
        Orden orden = ordenRepository.findById(ordenId).orElse(null);
        if (orden == null || orden.getEstado() != EstadoOrden.PENDIENTE_STOCK) {
            ignorado("stock.rechazado", ordenId, orden);
            return;
        }
        orden.setEstado(EstadoOrden.CANCELADA);
        orden.setObservacion("SIN_STOCK: " + (motivo == null ? "una prenda se agoto" : motivo));
        log.info("component=saga eventType=stock.rechazado ordenId={} estado=CANCELADA status=compensated", ordenId);
    }

    @Transactional
    public void alPagoAprobado(Long ordenId, String metodoPago) {
        Orden orden = ordenRepository.findById(ordenId).orElse(null);
        esperarSiAunNoHayStock(orden, "pago.aprobado", ordenId);
        if (orden == null || orden.getEstado() != EstadoOrden.PENDIENTE_PAGO) {
            ignorado("pago.aprobado", ordenId, orden);
            return;
        }
        aplicarMetodoPago(orden, metodoPago);
        orden.setEstado(EstadoOrden.PAGADA);
        orden.setObservacion("Pago aprobado. Preparando tu paquete");
        log.info("component=saga eventType=pago.aprobado ordenId={} estado=PAGADA status=processed", ordenId);
    }

    @Transactional
    public void alPagoRechazado(Long ordenId, String motivo, String metodoPago) {
        Orden orden = ordenRepository.findById(ordenId).orElse(null);
        esperarSiAunNoHayStock(orden, "pago.rechazado", ordenId);
        if (orden == null || orden.getEstado() != EstadoOrden.PENDIENTE_PAGO) {
            ignorado("pago.rechazado", ordenId, orden);
            return;
        }
        aplicarMetodoPago(orden, metodoPago);
        orden.setEstado(EstadoOrden.CANCELADA);
        orden.setObservacion("PAGO_RECHAZADO: " + (motivo == null ? "la pasarela rechazo el pago" : motivo));
        log.info("component=saga eventType=pago.rechazado ordenId={} estado=CANCELADA status=compensated", ordenId);
    }

    /** El comprador elige como pagar en la pantalla de pago: la orden se queda con ese metodo. */
    private void aplicarMetodoPago(Orden orden, String metodoPago) {
        if (metodoPago == null) {
            return;
        }
        try {
            orden.setMetodoPago(MetodoPago.valueOf(metodoPago));
        } catch (IllegalArgumentException desconocido) {
            log.warn("component=saga ordenId={} metodoPago={} status=ignored motivo=metodo_desconocido",
                    orden.getId(), metodoPago);
        }
    }

    private void esperarSiAunNoHayStock(Orden orden, String evento, Long ordenId) {
        if (orden != null && orden.getEstado() == EstadoOrden.PENDIENTE_STOCK) {
            throw new EventoFueraDeSecuenciaException(evento + " llego antes que stock.reservado para la orden " + ordenId);
        }
    }

    private void ignorado(String evento, Long ordenId, Orden orden) {
        log.info("component=saga eventType={} ordenId={} estadoActual={} status=ignored",
                evento, ordenId, orden == null ? "NO_EXISTE" : orden.getEstado());
    }
}
