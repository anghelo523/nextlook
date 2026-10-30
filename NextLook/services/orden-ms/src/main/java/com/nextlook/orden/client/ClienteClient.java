package com.nextlook.orden.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/** Cliente declarativo: el nombre "cliente-ms" se resuelve en Eureka (sin URL fija). */
@FeignClient(name = "cliente-ms", path = "/api/v1/clientes")
public interface ClienteClient {

    /** Usa el JWT del comprador (lo agrega FeignAuthConfig), por eso devuelve SU perfil. */
    @GetMapping("/me")
    ClienteRemoto obtenerMiPerfil();
}
