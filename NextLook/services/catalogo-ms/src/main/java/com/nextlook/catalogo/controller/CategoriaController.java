package com.nextlook.catalogo.controller;

import com.nextlook.catalogo.dto.CategoriaRequest;
import com.nextlook.catalogo.dto.CategoriaResponse;
import com.nextlook.catalogo.service.CatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CatalogoService catalogoService;

    @GetMapping
    public List<CategoriaResponse> listar() {
        return catalogoService.listarCategorias();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse crear(@Valid @RequestBody CategoriaRequest request) {
        return catalogoService.crearCategoria(request);
    }
}
