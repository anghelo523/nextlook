package com.nextlook.inventario.repository;

import com.nextlook.inventario.entity.OrdenProcesada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdenProcesadaRepository extends JpaRepository<OrdenProcesada, Long> {
    Optional<OrdenProcesada> findByOrdenId(Long ordenId);
}
