package com.nextlook.inventario.repository;

import com.nextlook.inventario.entity.EstadoReserva;
import com.nextlook.inventario.entity.ReservaStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaStockRepository extends JpaRepository<ReservaStock, Long> {
    List<ReservaStock> findByOrdenIdOrderByVarianteIdAsc(Long ordenId);
    List<ReservaStock> findByOrdenIdAndEstadoOrderByVarianteIdAsc(Long ordenId, EstadoReserva estado);
}
