package com.nextlook.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * S10 - El Gateway de NextLook es Spring Cloud Gateway Server **WebMVC** (Servlet, no WebFlux),
 * asi que puede usar el mismo OncePerRequestFilter que los microservicios.
 * Si la peticion no trae X-Trace-ID, lo crea aqui y lo AGREGA a la peticion que reenvia,
 * para que el microservicio de destino reutilice el mismo valor en vez de inventar otro.
 * Corre antes que Spring Security, asi tambien los 401/403 quedan con traceId.
 * Deja una linea component=http por peticion (sin /actuator) para que el traceId aparezca en Loki.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    public static final String TRACE_ID_HEADER = "X-Trace-ID";
    public static final String MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String recibido = request.getHeader(TRACE_ID_HEADER);
        String traceId = (recibido == null || recibido.isBlank()) ? UUID.randomUUID().toString() : recibido;
        long inicio = System.currentTimeMillis();
        try {
            MDC.put(MDC_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            chain.doFilter(new ConTraceId(request, traceId), response);
        } finally {
            if (!request.getRequestURI().startsWith("/actuator")) {
                log.info("component=http method={} path={} status={} durationMs={}", request.getMethod(),
                        request.getRequestURI(), response.getStatus(), System.currentTimeMillis() - inicio);
            }
            MDC.remove(MDC_KEY);
        }
    }

    /** Peticion "vista" con el header X-Trace-ID agregado: es lo que el Gateway copia al reenviar. */
    private static final class ConTraceId extends HttpServletRequestWrapper {
        private final String traceId;

        ConTraceId(HttpServletRequest request, String traceId) {
            super(request);
            this.traceId = traceId;
        }

        @Override
        public String getHeader(String name) {
            return TRACE_ID_HEADER.equalsIgnoreCase(name) ? traceId : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return TRACE_ID_HEADER.equalsIgnoreCase(name)
                    ? Collections.enumeration(List.of(traceId)) : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> nombres = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
            nombres.removeIf(TRACE_ID_HEADER::equalsIgnoreCase);
            nombres.add(TRACE_ID_HEADER);
            return Collections.enumeration(nombres);
        }
    }
}
