package com.nextlook.orden.controller;

import com.nextlook.orden.dto.CrearOrdenRequest;
import com.nextlook.orden.dto.OrdenResponse;
import com.nextlook.orden.service.OrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    /** 201 si la orden quedo validada (PENDIENTE_STOCK); 202 si quedo en CARRITO por el fallback. */
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody CrearOrdenRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        OrdenResponse orden = ordenService.crear(request, jwt.getSubject());
        return ResponseEntity.status(estadoHttp(orden)).body(orden);
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<OrdenResponse> confirmar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        OrdenResponse orden = ordenService.confirmarCarrito(id, jwt.getSubject());
        return ResponseEntity.status(estadoHttp(orden)).body(orden);
    }

    @PostMapping("/{id}/cancelar")
    public OrdenResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ordenService.cancelar(id, jwt.getSubject());
    }

    @GetMapping("/mias")
    public List<OrdenResponse> misOrdenes(@AuthenticationPrincipal Jwt jwt) {
        return ordenService.misOrdenes(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public OrdenResponse obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ordenService.obtener(id, jwt.getSubject(), esAdmin);
    }

    @GetMapping
    public List<OrdenResponse> listarTodas() {
        return ordenService.listarTodas();
    }

    private HttpStatus estadoHttp(OrdenResponse orden) {
        return "CARRITO".equals(orden.estado()) ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
    }
}
