export type EstadoPago = 'PENDIENTE' | 'LISTO_PARA_PAGAR' | 'APROBADO' | 'RECHAZADO' | 'ANULADO';
export type MetodoPago = 'TARJETA' | 'YAPE' | 'PLIN';

export interface Pago {
  id: number;
  ordenId: number;
  idCliente: number | null;
  monto: number;
  metodoPago: string;
  estado: EstadoPago;
  idTransaccion: string | null;
  motivo: string | null;
  fechaCreacion: string;
  fechaActualizacion: string;
}
