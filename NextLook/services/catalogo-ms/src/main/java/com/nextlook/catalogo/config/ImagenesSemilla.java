package com.nextlook.catalogo.config;

import com.nextlook.catalogo.repository.PrendaImagenRepository;
import com.nextlook.catalogo.repository.PrendaRepository;
import com.nextlook.catalogo.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Fotos de referencia de las prendas de prueba (resources/seed-imagenes/{idPrenda}.jpg, Unsplash License).
 * Flyway no carga binarios con comodidad, por eso se cargan al arrancar. Solo se cargan en prendas
 * SIN foto: nunca pisa una foto que el ADMIN ya subio.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImagenesSemilla implements ApplicationRunner {

    private final PrendaRepository prendaRepository;
    private final PrendaImagenRepository prendaImagenRepository;
    private final CatalogoService catalogoService;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Resource[] fotos = new PathMatchingResourcePatternResolver().getResources("classpath:seed-imagenes/*.jpg");
        int cargadas = 0;
        for (Resource foto : fotos) {
            String nombre = foto.getFilename();
            if (nombre == null || !nombre.matches("\\d+\\.jpg")) {
                continue;
            }
            Long prendaId = Long.valueOf(nombre.replace(".jpg", ""));
            if (!prendaRepository.existsById(prendaId) || prendaImagenRepository.existsById(prendaId)) {
                continue;
            }
            try (var entrada = foto.getInputStream()) {
                catalogoService.guardarImagen(prendaId, "image/jpeg", entrada.readAllBytes());
                cargadas++;
            }
        }
        log.info("component=catalogo action=imagenesSemilla disponibles={} cargadas={}", fotos.length, cargadas);
    }
}
