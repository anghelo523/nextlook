import { environment } from '../../environments/environment';

/**
 * catalogo-ms devuelve la foto como ruta relativa al Gateway (/api/v1/productos/{id}/imagen?v=...).
 * La SPA solo conoce el Gateway (S04), asi que se completa con su direccion.
 */
export function urlImagen(ruta: string | null | undefined): string | null {
  if (!ruta) return null;
  return /^https?:\/\//.test(ruta) ? ruta : `${environment.apiBaseUrl}${ruta}`;
}

/** Formato de moneda corto y consistente en toda la tienda: S/ 129.90 */
export function soles(monto: number | null | undefined): string {
  return `S/ ${(monto ?? 0).toFixed(2)}`;
}
