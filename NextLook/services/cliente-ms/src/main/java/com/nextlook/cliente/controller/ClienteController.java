package com.nextlook.cliente.controller;

import com.nextlook.cliente.dto.*;
import com.nextlook.cliente.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    // ---------- CLIENTE: su propio perfil (identidad tomada del JWT, S07) ----------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse registrarMiPerfil(@Valid @RequestBody PerfilRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        return clienteService.registrarPerfil(jwt.getSubject(), emailDelToken(jwt), request);
    }

    @GetMapping("/me")
    public ClienteResponse miPerfil(@AuthenticationPrincipal Jwt jwt) {
        return clienteService.miPerfil(jwt.getSubject());
    }

    @PutMapping("/me")
    public ClienteResponse actualizarMiPerfil(@Valid @RequestBody PerfilRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        return clienteService.actualizarPerfil(jwt.getSubject(), request);
    }

    @PostMapping("/me/direcciones")
    @ResponseStatus(HttpStatus.CREATED)
    public DireccionResponse agregarDireccion(@Valid @RequestBody DireccionRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        return clienteService.agregarDireccion(jwt.getSubject(), request);
    }

    @GetMapping("/me/direcciones")
    public List<DireccionResponse> misDirecciones(@AuthenticationPrincipal Jwt jwt) {
        return clienteService.misDirecciones(jwt.getSubject());
    }

    // ---------- ADMIN: gestion global ----------

    @GetMapping
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    private String emailDelToken(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("preferred_username");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El token no trae email ni preferred_username");
        }
        return email.trim().toLowerCase();
    }
}
