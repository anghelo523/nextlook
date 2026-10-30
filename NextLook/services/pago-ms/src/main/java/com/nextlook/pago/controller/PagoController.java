package com.nextlook.pago.controller;

import com.nextlook.pago.dto.PagoResponse;
import com.nextlook.pago.dto.ProcesarPagoRequest;
import com.nextlook.pago.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping("/procesar")
    public PagoResponse procesar(@Valid @RequestBody ProcesarPagoRequest request, @AuthenticationPrincipal Jwt jwt) {
        return pagoService.procesar(request, jwt.getSubject());
    }

    @GetMapping("/orden/{ordenId}")
    public PagoResponse deOrden(@PathVariable Long ordenId, @AuthenticationPrincipal Jwt jwt, Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return pagoService.deOrden(ordenId, jwt.getSubject(), esAdmin);
    }

    @GetMapping
    public List<PagoResponse> listar() {
        return pagoService.listar();
    }
}
