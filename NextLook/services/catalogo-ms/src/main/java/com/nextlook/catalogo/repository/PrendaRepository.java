package com.nextlook.catalogo.repository;

import com.nextlook.catalogo.entity.Prenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrendaRepository extends JpaRepository<Prenda, Long> {
    List<Prenda> findByActivoTrueOrderByIdAsc();
    List<Prenda> findByActivoTrueAndCategoriaIdOrderByIdAsc(Long categoriaId);
}
