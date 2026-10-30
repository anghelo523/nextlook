package com.nextlook.catalogo.repository;

import com.nextlook.catalogo.entity.Variante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VarianteRepository extends JpaRepository<Variante, Long> {
    boolean existsBySku(String sku);
}
