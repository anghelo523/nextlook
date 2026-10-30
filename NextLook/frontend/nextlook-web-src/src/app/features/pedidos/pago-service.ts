import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MetodoPago, Pago } from './pago.model';

@Injectable({ providedIn: 'root' })
export class PagoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/v1/pagos`;

  /**
   * 404 NO es un error: significa "pago-ms todavia no consumio orden.creada".
   * Se traduce a null para que la pantalla lo muestre como "preparando el cobro".
   */
  deOrden(ordenId: number): Observable<Pago | null> {
    return this.http.get<Pago>(`${this.base}/orden/${ordenId}`).pipe(
      catchError((err: HttpErrorResponse) => (err.status === 404 ? of(null) : throwError(() => err))),
    );
  }

  /** Pasarela simulada (S08): titular APRO aprueba; FUND u OTHE rechazan. El metodo se elige al pagar. */
  procesar(ordenId: number, titular: string, metodoPago: MetodoPago): Observable<Pago> {
    return this.http.post<Pago>(`${this.base}/procesar`, { ordenId, titular, metodoPago });
  }

  listar(): Observable<Pago[]> {
    return this.http.get<Pago[]>(this.base);
  }
}
