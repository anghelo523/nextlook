package com.nextlook.inventario.service;

import com.nextlook.inventario.dto.ReservaResponse;
import com.nextlook.inventario.dto.StockRequest;
import com.nextlook.inventario.dto.StockResponse;
import com.nextlook.inventario.entity.StockVariante;
import com.nextlook.inventario.exception.ReglaNegocioException;
import com.nextlook.inventario.exception.ResourceNotFoundException;
import com.nextlook.inventario.repository.ReservaStockRepository;
import com.nextlook.inventario.repository.StockVarianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockVarianteRepository stockRepository;
    private final ReservaStockRepository reservaRepository;

    @Transactional(readOnly = true)
    public List<StockResponse> listar() {
        return stockRepository.findAllByOrderByVarianteIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StockResponse obtener(Long varianteId) {
        return toResponse(stockRepository.findById(varianteId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay stock registrado para la variante " + varianteId)));
    }

    @Transactional
    public StockResponse registrarOAjustar(Long varianteId, StockRequest request) {
        if (stockRepository.existsBySkuAndVarianteIdNot(request.sku(), varianteId)) {
            throw new ReglaNegocioException("El SKU " + request.sku() + " ya pertenece a otra variante");
        }
        StockVariante stock = stockRepository.bloquear(varianteId)
                .orElseGet(() -> StockVariante.builder().varianteId(varianteId).reservado(0).build());
        stock.setSku(request.sku());
        stock.setDisponible(request.disponible());
        return toResponse(stockRepository.save(stock));
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> reservasDeOrden(Long ordenId) {
        return reservaRepository.findByOrdenIdOrderByVarianteIdAsc(ordenId).stream()
                .map(r -> new ReservaResponse(r.getId(), r.getOrdenId(), r.getVarianteId(), r.getCantidad(),
                        r.getEstado().name(), r.getFechaActualizacion()))
                .toList();
    }

    private StockResponse toResponse(StockVariante s) {
        return new StockResponse(s.getVarianteId(), s.getSku(), s.getDisponible(), s.getReservado(), s.getFechaActualizacion());
    }
}
