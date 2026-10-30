package com.nextlook.cliente.repository;

import com.nextlook.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByKeycloakId(String keycloakId);
    boolean existsByKeycloakId(String keycloakId);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByDni(String dni);
}
