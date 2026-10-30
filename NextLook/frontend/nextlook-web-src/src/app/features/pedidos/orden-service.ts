import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CrearOrdenRequest, Orden } from './orden.model';

export interface OrdenCreada {
  orden: Orden;
  /** 201 = validada (PENDIENTE_STOCK); 202 = quedo en CARRITO por el fallback del Circuit Breaker (S06). */
  httpStatus: number;
  traceId: string | null;
}

@Injectable({ providedIn: 'root' })
export class OrdenService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/v1/ordenes`;

  crear(request: CrearOrdenRequest): Observable<OrdenCreada> {
    return this.http.post<Orden>(this.base, request, { observe: 'response' }).pipe(
      map((resp) => ({
        orden: resp.body as Orden,
        httpStatus: resp.status,
        // legible gracias a exposedHeaders en el CorsConfig del Gateway
        traceId: resp.headers.get('X-Trace-ID'),
      })),
    );
  }

  /** Filtrado por dueno EN EL SERVIDOR (orden-ms compara con el sub del JWT). */
  mias(): Observable<Orden[]> {
    return this.http.get<Orden[]>(`${this.base}/mias`);
  }

  obtener(id: number): Observable<Orden> {
    return this.http.get<Orden>(`${this.base}/${id}`);
  }

  confirmarCarrito(id: number): Observable<Orden> {
    return this.http.post<Orden>(`${this.base}/${id}/confirmar`, {});
  }

  /** El comprador desiste: orden-ms la cancela y, si ya estaba separada, publica orden.cancelada. */
  cancelar(id: number): Observable<Orden> {
    return this.http.post<Orden>(`${this.base}/${id}/cancelar`, {});
  }
}
