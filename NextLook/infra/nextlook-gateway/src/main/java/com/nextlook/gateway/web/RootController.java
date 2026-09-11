package com.nextlook.gateway.web;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * El Gateway no expone negocio propio en "/", solo enruta "/api/v1/**" hacia los
 * microservicios (ver nextlook-gateway-{dev,prod}.yml). Sin este controlador, pegarle
 * a "/" da Whitelabel 404 - lo cual es correcto, pero confunde. Esto es solo una pagina
 * de bienvenida con links utiles.
 */
@RestController
public class RootController {

	@GetMapping("/")
	public Map<String, Object> root() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("service", "nextlook-gateway");
		body.put("status", "UP");
		body.put("routes", "/api/v1/{categorias,productos,inventario,resenas,ordenes,pagos,clientes,envios,notificaciones}/**");
		body.put("health", "/actuator/health");
		body.put("eureka", "http://localhost:18761");
		return body;
	}

}
