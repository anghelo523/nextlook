export type EstadoOrden = 'CARRITO' | 'PENDIENTE_STOCK' | 'PENDIENTE_PAGO' | 'PAGADA' | 'CANCELADA';

export interface OrdenItem {
  varianteId: number;
  nombrePrenda: string | null;
  talla: string | null;
  color: string | null;
  precioUnitario: number | null;
  cantidad: number;
  subtotal: number;
}

export interface Orden {
  id: number;
  idCliente: number | null;
  nombreCliente: string | null;
  direccionEnvio: string | null;
  estado: EstadoOrden;
  metodoPago: string;
  total: number;
  observacion: string | null;
  items: OrdenItem[];
  fechaCreacion: string;
  fechaActualizacion: string;
}

export interface CrearOrdenRequest {
  metodoPago: 'TARJETA' | 'YAPE' | 'PLIN';
  items: { varianteId: number; cantidad: number }[];
}
