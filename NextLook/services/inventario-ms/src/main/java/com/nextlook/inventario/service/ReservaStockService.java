package com.nextlook.inventario.service;

import com.nextlook.inventario.entity.EstadoReserva;
import com.nextlook.inventario.entity.OrdenProcesada;
import com.nextlook.inventario.entity.ReservaStock;
import com.nextlook.inventario.entity.StockVariante;
import com.nextlook.inventario.event.OrdenCreadaEvento;
import com.nextlook.inventario.event.StockEvento;
import com.nextlook.inventario.event.TipoEvento;
import com.nextlook.inventario.messaging.EventoPublisher;
import com.nextlook.inventario.repository.OrdenProcesadaRepository;
import com.nextlook.inventario.repository.ReservaStockRepository;
import com.nextlook.inventario.repository.StockVarianteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaStockService {

    private final StockVarianteRepository stockRepository;
    private final ReservaStockRepository reservaRepository;
    private final OrdenProcesadaRepository ordenProcesadaRepository;
    private final EventoPublisher eventoPublisher;

    @Value("${app.kafka.topic.inventario}")
    private String topicInventario;

    /**
     * S08/S09 - Reserva TODO o NADA. Si una sola talla no alcanza, no se separa ninguna prenda.
     * Capa 1 de idempotencia: si la orden ya figura en orden_procesada, se ignora.
     * Capa 2: el UNIQUE(orden_id) de orden_procesada; el listener atrapa la violacion.
     */
    @Transactional
    public void reservarParaOrden(OrdenCreadaEvento evento) {
        Long ordenId = evento.ordenId();
        if (ordenProcesadaRepository.findByOrdenId(ordenId).isPresent()) {
            log.info("component=inventario eventType=orden.creada ordenId={} status=ignored motivo=ya_procesada", ordenId);
            return;
        }

        // Agrupa y ordena por varianteId: los bloqueos siempre en el mismo orden evitan deadlocks.
        SortedMap<Long, Integer> pedido = new TreeMap<>();
        evento.items().forEach(i -> pedido.merge(i.varianteId(), i.cantidad(), Integer::sum));

        List<String> faltantes = new ArrayList<>();
        Map<Long, StockVariante> bloqueados = new LinkedHashMap<>();
        pedido.forEach((varianteId, cantidad) -> {
            Optional<StockVariante> stock = stockRepository.bloquear(varianteId);
            if (stock.isEmpty()) {
                faltantes.add("variante " + varianteId + " sin stock registrado");
            } else if (stock.get().getDisponible() < cantidad) {
                faltantes.add(stock.get().getSku() + " pide " + cantidad + ", hay " + stock.get().getDisponible());
            } else {
                bloqueados.put(varianteId, stock.get());
            }
        });

        if (!faltantes.isEmpty()) {
            String motivo = String.join("; ", faltantes);
            ordenProcesadaRepository.saveAndFlush(OrdenProcesada.builder()
                    .ordenId(ordenId).resultado("RECHAZADO").motivo(recortar(motivo)).build());
            eventoPublisher.publicarTrasCommit(topicInventario, ordenId, TipoEvento.STOCK_RECHAZADO,
                    StockEvento.rechazado(ordenId, motivo));
            log.info("component=inventario eventType=orden.creada ordenId={} status=processed resultado=RECHAZADO motivo=\"{}\"",
                    ordenId, motivo);
            return;
        }

        ordenProcesadaRepository.saveAndFlush(OrdenProcesada.builder().ordenId(ordenId).resultado("RESERVADO").build());
        pedido.forEach((varianteId, cantidad) -> {
            StockVariante stock = bloqueados.get(varianteId);
            stock.setDisponible(stock.getDisponible() - cantidad);
            stock.setReservado(stock.getReservado() + cantidad);
            reservaRepository.save(ReservaStock.builder()
                    .ordenId(ordenId).varianteId(varianteId).cantidad(cantidad).estado(EstadoReserva.RESERVADA).build());
        });
        eventoPublisher.publicarTrasCommit(topicInventario, ordenId, TipoEvento.STOCK_RESERVADO, StockEvento.reservado(ordenId));
        log.info("component=inventario eventType=orden.creada ordenId={} status=processed resultado=RESERVADO variantes={}",
                ordenId, pedido.keySet());
    }

    /** pago.aprobado: las prendas separadas pasan a vendidas (salen de "reservado"). */
    @Transactional
    public void confirmarReservas(Long ordenId) {
        List<ReservaStock> reservas = reservaRepository.findByOrdenIdAndEstadoOrderByVarianteIdAsc(ordenId, EstadoReserva.RESERVADA);
        if (reservas.isEmpty()) {
            log.info("component=inventario eventType=pago.aprobado ordenId={} status=ignored motivo=sin_reservas_activas", ordenId);
            return;
        }
        for (ReservaStock reserva : reservas) {
            StockVariante stock = stockRepository.bloquear(reserva.getVarianteId()).orElseThrow();
            stock.setReservado(stock.getReservado() - reserva.getCantidad());
            reserva.setEstado(EstadoReserva.CONFIRMADA);
        }
        log.info("component=inventario eventType=pago.aprobado ordenId={} reservas={} status=processed", ordenId, reservas.size());
    }

    /**
     * S09 - Compensacion de pago.rechazado u orden.cancelada (el comprador desistio):
     * las prendas vuelven a estar disponibles.
     */
    @Transactional
    public void liberarReservas(Long ordenId, String eventType) {
        List<ReservaStock> reservas = reservaRepository.findByOrdenIdAndEstadoOrderByVarianteIdAsc(ordenId, EstadoReserva.RESERVADA);
        if (reservas.isEmpty()) {
            // Misma guarda que el camino feliz: un rechazo repetido no devuelve stock dos veces.
            log.info("component=inventario eventType={} ordenId={} status=ignored motivo=sin_reservas_activas", eventType, ordenId);
            return;
        }
        for (ReservaStock reserva : reservas) {
            StockVariante stock = stockRepository.bloquear(reserva.getVarianteId()).orElseThrow();
            stock.setReservado(stock.getReservado() - reserva.getCantidad());
            stock.setDisponible(stock.getDisponible() + reserva.getCantidad());
            reserva.setEstado(EstadoReserva.LIBERADA);
        }
        log.info("component=inventario eventType={} ordenId={} reservas={} status=compensated", eventType, ordenId, reservas.size());
    }

    private String recortar(String texto) {
        return texto.length() <= 300 ? texto : texto.substring(0, 297) + "...";
    }
}
