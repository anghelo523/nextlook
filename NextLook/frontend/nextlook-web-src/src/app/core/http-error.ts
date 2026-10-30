import { HttpErrorResponse } from '@angular/common/http';

/** Los microservicios responden ProblemDetail ({ detail: "..." }); se muestra ese texto. */
export function mensajeDeError(err: unknown, porDefecto: string): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'No hay conexion con el Gateway (¿esta levantado? ¿CORS?).';
    if (err.status === 403) return 'Tu cuenta no tiene permiso para esta accion.';
    const detalle = (err.error as { detail?: string } | null)?.detail;
    if (detalle) return detalle;
  }
  return porDefecto;
}
