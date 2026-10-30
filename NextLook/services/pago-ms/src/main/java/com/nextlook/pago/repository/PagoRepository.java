package com.nextlook.pago.repository;

import com.nextlook.pago.entity.Pago;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByOrdenId(Long ordenId);

    /** Evita que dos clics en "Pagar" cobren la misma orden dos veces. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pago p where p.ordenId = :ordenId")
    Optional<Pago> bloquearPorOrden(@Param("ordenId") Long ordenId);

    List<Pago> findAllByOrderByIdDesc();
}
