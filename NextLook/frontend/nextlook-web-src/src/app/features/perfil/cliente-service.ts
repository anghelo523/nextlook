import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cliente, Direccion, DireccionRequest, PerfilRequest } from './cliente.model';

/** cliente-ms identifica al comprador por el "sub" del token: nunca se envia un id de cliente. */
@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/v1/clientes`;

  miPerfil(): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.base}/me`);
  }

  registrar(perfil: PerfilRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.base, perfil);
  }

  agregarDireccion(direccion: DireccionRequest): Observable<Direccion> {
    return this.http.post<Direccion>(`${this.base}/me/direcciones`, direccion);
  }
}
