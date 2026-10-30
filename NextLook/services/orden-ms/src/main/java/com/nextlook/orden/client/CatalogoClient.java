package com.nextlook.orden.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogo-ms", path = "/api/v1/productos")
public interface CatalogoClient {

    @GetMapping("/variantes/{varianteId}")
    VarianteRemota obtenerVariante(@PathVariable("varianteId") Long varianteId);
}
