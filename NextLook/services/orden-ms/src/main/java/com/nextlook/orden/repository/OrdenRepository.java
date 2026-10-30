package com.nextlook.orden.repository;

import com.nextlook.orden.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByKeycloakIdOrderByIdDesc(String keycloakId);
    List<Orden> findAllByOrderByIdDesc();
}
