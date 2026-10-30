package com.nextlook.inventario.controller;

import com.nextlook.inventario.dto.ReservaResponse;
import com.nextlook.inventario.dto.StockRequest;
import com.nextlook.inventario.dto.StockResponse;
import com.nextlook.inventario.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Solo ADMIN (gateway + SecurityConfig). El stock lo mueven los eventos de la Saga, no el comprador. */
@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final StockService stockService;

    @GetMapping
    public List<StockResponse> listar() {
        return stockService.listar();
    }

    @GetMapping("/{varianteId}")
    public StockResponse obtener(@PathVariable Long varianteId) {
        return stockService.obtener(varianteId);
    }

    @PutMapping("/{varianteId}")
    public StockResponse registrarOAjustar(@PathVariable Long varianteId, @Valid @RequestBody StockRequest request) {
        return stockService.registrarOAjustar(varianteId, request);
    }

    @GetMapping("/reservas/{ordenId}")
    public List<ReservaResponse> reservas(@PathVariable Long ordenId) {
        return stockService.reservasDeOrden(ordenId);
    }
}
