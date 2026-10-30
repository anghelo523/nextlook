import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map, shareReplay } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Categoria, Prenda, PrendaRequest, Stock } from './prenda.model';

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/v1`;
  private todas$?: Observable<Prenda[]>;

  categorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.base}/categorias`);
  }

  prendas(categoriaId?: number): Observable<Prenda[]> {
    const filtro = categoriaId ? `?categoriaId=${categoriaId}` : '';
    return this.http.get<Prenda[]>(`${this.base}/productos${filtro}`);
  }

  /** Catalogo completo compartido (portada, fotos de "Mis pedidos"). Se invalida cuando el ADMIN lo cambia. */
  todas(): Observable<Prenda[]> {
    this.todas$ ??= this.prendas().pipe(shareReplay({ bufferSize: 1, refCount: false }));
    return this.todas$;
  }

  /** varianteId -> foto de su prenda. Las ordenes no guardan la foto: se cruza con el catalogo. */
  fotosPorVariante(): Observable<Map<number, string | null>> {
    return this.todas().pipe(
      map((prendas) => new Map(prendas.flatMap((p) => p.variantes.map((v) => [v.id, p.imagenUrl] as const)))),
    );
  }

  // ---------------- ADMIN (Gateway y catalogo-ms exigen rol ADMIN) ----------------

  crearPrenda(request: PrendaRequest): Observable<Prenda> {
    this.todas$ = undefined;
    return this.http.post<Prenda>(`${this.base}/productos`, request);
  }

  /** La foto viaja tal cual (image/jpeg), sin multipart: el Gateway la reenvia como cualquier cuerpo. */
  subirImagen(prendaId: number, foto: Blob): Observable<Prenda> {
    this.todas$ = undefined;
    return this.http.put<Prenda>(`${this.base}/productos/${prendaId}/imagen`, foto, {
      headers: new HttpHeaders({ 'Content-Type': foto.type || 'image/jpeg' }),
    });
  }

  retirar(prendaId: number): Observable<void> {
    this.todas$ = undefined;
    return this.http.delete<void>(`${this.base}/productos/${prendaId}`);
  }

  crearCategoria(nombre: string): Observable<Categoria> {
    return this.http.post<Categoria>(`${this.base}/categorias`, { nombre });
  }

  stock(): Observable<Stock[]> {
    return this.http.get<Stock[]>(`${this.base}/inventario`);
  }

  /** inventario-ms crea la fila de stock si la variante es nueva. */
  registrarStock(varianteId: number, sku: string, disponible: number): Observable<Stock> {
    return this.http.put<Stock>(`${this.base}/inventario/${varianteId}`, { sku, disponible });
  }
}
