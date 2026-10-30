package com.nextlook.cliente.service;

import com.nextlook.cliente.dto.*;
import com.nextlook.cliente.entity.Cliente;
import com.nextlook.cliente.entity.Direccion;
import com.nextlook.cliente.exception.ReglaNegocioException;
import com.nextlook.cliente.exception.ResourceNotFoundException;
import com.nextlook.cliente.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponse registrarPerfil(String keycloakId, String email, PerfilRequest request) {
        if (clienteRepository.existsByKeycloakId(keycloakId)) {
            throw new ReglaNegocioException("Ya registraste tu perfil de cliente");
        }
        if (clienteRepository.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("Ya existe un cliente con el email " + email);
        }
        if (request.dni() != null && clienteRepository.existsByDni(request.dni())) {
            throw new ReglaNegocioException("Ya existe un cliente con el DNI " + request.dni());
        }
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .keycloakId(keycloakId)
                .email(email.toLowerCase())
                .nombres(request.nombres().trim())
                .apellidos(request.apellidos().trim())
                .dni(request.dni())
                .telefono(request.telefono())
                .build());
        log.info("component=cliente action=registrarPerfil clienteId={} status=created", cliente.getId());
        return toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponse miPerfil(String keycloakId) {
        return toResponse(buscarPorKeycloak(keycloakId));
    }

    @Transactional
    public ClienteResponse actualizarPerfil(String keycloakId, PerfilRequest request) {
        Cliente cliente = buscarPorKeycloak(keycloakId);
        cliente.setNombres(request.nombres().trim());
        cliente.setApellidos(request.apellidos().trim());
        cliente.setDni(request.dni());
        cliente.setTelefono(request.telefono());
        return toResponse(cliente);
    }

    @Transactional
    public DireccionResponse agregarDireccion(String keycloakId, DireccionRequest request) {
        Cliente cliente = buscarPorKeycloak(keycloakId);
        // La primera direccion siempre queda como principal; si se marca otra, deja de serlo la anterior.
        boolean principal = cliente.getDirecciones().isEmpty() || Boolean.TRUE.equals(request.principal());
        if (principal) {
            cliente.getDirecciones().forEach(d -> d.setPrincipal(false));
        }
        Direccion direccion = Direccion.builder()
                .cliente(cliente)
                .alias(request.alias())
                .direccion(request.direccion())
                .distrito(request.distrito())
                .ciudad(request.ciudad())
                .referencia(request.referencia())
                .principal(principal)
                .build();
        cliente.getDirecciones().add(direccion);
        clienteRepository.saveAndFlush(cliente);
        return toResponse(direccion);
    }

    @Transactional(readOnly = true)
    public List<DireccionResponse> misDirecciones(String keycloakId) {
        return buscarPorKeycloak(keycloakId).getDirecciones().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return toResponse(clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id)));
    }

    private Cliente buscarPorKeycloak(String keycloakId) {
        return clienteRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Todavia no registraste tu perfil de cliente (POST /api/v1/clientes)"));
    }

    private DireccionResponse toResponse(Direccion d) {
        return new DireccionResponse(d.getId(), d.getAlias(), d.getDireccion(), d.getDistrito(),
                d.getCiudad(), d.getReferencia(), d.getPrincipal());
    }

    private ClienteResponse toResponse(Cliente c) {
        List<DireccionResponse> direcciones = c.getDirecciones().stream().map(this::toResponse).toList();
        DireccionResponse principal = direcciones.stream()
                .filter(d -> Boolean.TRUE.equals(d.principal())).findFirst().orElse(null);
        return new ClienteResponse(c.getId(), c.getNombres(), c.getApellidos(), c.getEmail(), c.getDni(),
                c.getTelefono(), c.getFechaRegistro(), direcciones, principal);
    }
}
