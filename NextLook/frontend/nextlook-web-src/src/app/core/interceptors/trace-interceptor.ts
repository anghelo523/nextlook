import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

/**
 * S10/S11 - Un X-Trace-ID por peticion, generado en el navegador. El Gateway y cada
 * CorrelationIdFilter lo reutilizan, y viaja tambien por Feign y por Kafka: el mismo valor
 * se busca en Grafana con el panel "Seguir una compra por traceId".
 */
export const traceInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiBaseUrl) || req.headers.has('X-Trace-ID')) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { 'X-Trace-ID': 'web-' + crypto.randomUUID() } }));
};
