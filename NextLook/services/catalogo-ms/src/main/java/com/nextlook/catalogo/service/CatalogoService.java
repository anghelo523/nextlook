package com.nextlook.catalogo.service;

import com.nextlook.catalogo.dto.*;
import com.nextlook.catalogo.entity.Categoria;
import com.nextlook.catalogo.entity.Prenda;
import com.nextlook.catalogo.entity.PrendaImagen;
import com.nextlook.catalogo.entity.Variante;
import com.nextlook.catalogo.exception.ReglaNegocioException;
import com.nextlook.catalogo.exception.ResourceNotFoundException;
import com.nextlook.catalogo.repository.CategoriaRepository;
import com.nextlook.catalogo.repository.PrendaImagenRepository;
import com.nextlook.catalogo.repository.PrendaRepository;
import com.nextlook.catalogo.repository.VarianteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final CategoriaRepository categoriaRepository;
    private final PrendaRepository prendaRepository;
    private final VarianteRepository varianteRepository;
    private final PrendaImagenRepository prendaImagenRepository;

    // ---------------- categorias ----------------

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new ReglaNegocioException("Ya existe la categoria " + request.nombre());
        }
        return toResponse(categoriaRepository.save(Categoria.builder().nombre(request.nombre().trim()).build()));
    }

    // ---------------- prendas ----------------

    @Transactional(readOnly = true)
    public List<PrendaResponse> listarPrendas(Long categoriaId) {
        List<Prenda> prendas = categoriaId == null
                ? prendaRepository.findByActivoTrueOrderByIdAsc()
                : prendaRepository.findByActivoTrueAndCategoriaIdOrderByIdAsc(categoriaId);
        return prendas.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PrendaResponse obtenerPrenda(Long id) {
        return toResponse(buscarPrenda(id));
    }

    @Transactional
    public PrendaResponse crearPrenda(PrendaRequest request) {
        Prenda prenda = Prenda.builder()
                .nombre(request.nombre().trim())
                .descripcion(request.descripcion())
                .marca(request.marca())
                .genero(request.genero())
                .precio(request.precio())
                .categoria(buscarCategoria(request.categoriaId()))
                .build();
        if (request.variantes() != null) {
            for (VarianteRequest v : request.variantes()) {
                if (varianteRepository.existsBySku(v.sku())) {
                    throw new ReglaNegocioException("El SKU " + v.sku() + " ya existe");
                }
                prenda.getVariantes().add(Variante.builder()
                        .prenda(prenda).talla(v.talla()).color(v.color()).sku(v.sku()).build());
            }
        }
        Prenda guardada = prendaRepository.save(prenda);
        log.info("component=catalogo action=crearPrenda prendaId={} variantes={} status=created",
                guardada.getId(), guardada.getVariantes().size());
        return toResponse(guardada);
    }

    @Transactional
    public PrendaResponse actualizarPrenda(Long id, PrendaRequest request) {
        Prenda prenda = buscarPrenda(id);
        prenda.setNombre(request.nombre().trim());
        prenda.setDescripcion(request.descripcion());
        prenda.setMarca(request.marca());
        prenda.setGenero(request.genero());
        prenda.setPrecio(request.precio());
        prenda.setCategoria(buscarCategoria(request.categoriaId()));
        return toResponse(prenda);
    }

    /** Baja logica: la prenda deja de venderse, pero las ordenes historicas siguen apuntando a ella. */
    @Transactional
    public void desactivarPrenda(Long id) {
        Prenda prenda = buscarPrenda(id);
        prenda.setActivo(false);
        prenda.getVariantes().forEach(v -> v.setActivo(false));
    }

    // ---------------- fotos ----------------

    public static final long MAX_BYTES_IMAGEN = 3 * 1024 * 1024;
    private static final List<String> TIPOS_IMAGEN = List.of("image/jpeg", "image/png", "image/webp");

    @Transactional(readOnly = true)
    public PrendaImagen obtenerImagen(Long prendaId) {
        return prendaImagenRepository.findById(prendaId)
                .orElseThrow(() -> new ResourceNotFoundException("La prenda " + prendaId + " no tiene foto"));
    }

    /** ADMIN sube o reemplaza la foto. El cuerpo es la imagen tal cual (Content-Type image/*), sin multipart. */
    @Transactional
    public PrendaResponse guardarImagen(Long prendaId, String tipo, byte[] contenido) {
        Prenda prenda = buscarPrenda(prendaId);
        String tipoNormalizado = tipo == null ? "" : tipo.split(";")[0].trim().toLowerCase();
        if (!TIPOS_IMAGEN.contains(tipoNormalizado)) {
            throw new ReglaNegocioException("La foto debe ser JPG, PNG o WEBP");
        }
        if (contenido == null || contenido.length == 0) {
            throw new ReglaNegocioException("La foto esta vacia");
        }
        if (contenido.length > MAX_BYTES_IMAGEN) {
            throw new ReglaNegocioException("La foto pesa mas de 3 MB");
        }
        PrendaImagen imagen = prendaImagenRepository.findById(prendaId)
                .orElseGet(() -> PrendaImagen.builder().prendaId(prendaId).build());
        imagen.setTipo(tipoNormalizado);
        imagen.setContenido(contenido);
        prendaImagenRepository.save(imagen);
        prenda.setImagenVersion(System.currentTimeMillis());
        log.info("component=catalogo action=guardarImagen prendaId={} bytes={} tipo={} status=saved",
                prendaId, contenido.length, tipoNormalizado);
        return toResponse(prenda);
    }

    // ---------------- variantes (consulta de orden-ms) ----------------

    @Transactional(readOnly = true)
    public VarianteDetalleResponse obtenerVariante(Long varianteId) {
        Variante v = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + varianteId));
        Prenda p = v.getPrenda();
        return new VarianteDetalleResponse(v.getId(), p.getId(), p.getNombre(), v.getTalla(), v.getColor(),
                v.getSku(), p.getPrecio(), Boolean.TRUE.equals(p.getActivo()) && Boolean.TRUE.equals(v.getActivo()));
    }

    // ---------------- helpers ----------------

    private Prenda buscarPrenda(Long id) {
        return prendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prenda no encontrada: " + id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + id));
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre());
    }

    private PrendaResponse toResponse(Prenda p) {
        return new PrendaResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getMarca(),
                p.getGenero().name(), p.getPrecio(), p.getActivo(), toResponse(p.getCategoria()),
                p.getVariantes().stream()
                        .map(v -> new VarianteResponse(v.getId(), v.getTalla(), v.getColor(), v.getSku(), v.getActivo()))
                        .toList(),
                p.getImagenVersion() == null ? null
                        : "/api/v1/productos/" + p.getId() + "/imagen?v=" + p.getImagenVersion());
    }
}
