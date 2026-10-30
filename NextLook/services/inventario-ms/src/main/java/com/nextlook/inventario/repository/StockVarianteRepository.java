package com.nextlook.inventario.repository;

import com.nextlook.inventario.entity.StockVariante;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockVarianteRepository extends JpaRepository<StockVariante, Long> {

    /** SELECT ... FOR UPDATE: dos ordenes por la misma talla no pueden vender la misma ultima unidad. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockVariante s where s.varianteId = :id")
    Optional<StockVariante> bloquear(@Param("id") Long varianteId);

    List<StockVariante> findAllByOrderByVarianteIdAsc();

    boolean existsBySkuAndVarianteIdNot(String sku, Long varianteId);
}
