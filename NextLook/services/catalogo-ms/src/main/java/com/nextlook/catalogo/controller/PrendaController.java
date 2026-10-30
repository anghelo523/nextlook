package com.nextlook.catalogo.controller;

import com.nextlook.catalogo.dto.PrendaRequest;
import com.nextlook.catalogo.dto.PrendaResponse;
import com.nextlook.catalogo.dto.VarianteDetalleResponse;
import com.nextlook.catalogo.entity.PrendaImagen;
import com.nextlook.catalogo.service.CatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class PrendaController {

    private final CatalogoService catalogoService;

    @GetMapping
    public List<PrendaResponse> listar(@RequestParam(required = false) Long categoriaId) {
        return catalogoService.listarPrendas(categoriaId);
    }

    @GetMapping("/{id}")
    public PrendaResponse obtener(@PathVariable Long id) {
        return catalogoService.obtenerPrenda(id);
    }

    /** Consulta que usa orden-ms por Feign para validar la talla/color y copiar el precio. */
    @GetMapping("/variantes/{varianteId}")
    public VarianteDetalleResponse obtenerVariante(@PathVariable Long varianteId) {
        return catalogoService.obtenerVariante(varianteId);
    }

    /** Publico como el resto del catalogo. La URL lleva ?v=version, asi que se puede cachear sin miedo. */
    @GetMapping("/{id}/imagen")
    public ResponseEntity<byte[]> imagen(@PathVariable Long id) {
        PrendaImagen imagen = catalogoService.obtenerImagen(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.getTipo()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(imagen.getContenido());
    }

    /** Solo ADMIN (SecurityConfig + Gateway: todo lo que no es GET en /api/v1/productos/**). */
    @PutMapping(value = "/{id}/imagen", consumes = {"image/jpeg", "image/png", "image/webp"})
    public PrendaResponse subirImagen(@PathVariable Long id,
                                      @RequestHeader(HttpHeaders.CONTENT_TYPE) String tipo,
                                      @RequestBody byte[] contenido) {
        return catalogoService.guardarImagen(id, tipo, contenido);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrendaResponse crear(@Valid @RequestBody PrendaRequest request) {
        return catalogoService.crearPrenda(request);
    }

    @PutMapping("/{id}")
    public PrendaResponse actualizar(@PathVariable Long id, @Valid @RequestBody PrendaRequest request) {
        return catalogoService.actualizarPrenda(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        catalogoService.desactivarPrenda(id);
    }
}
