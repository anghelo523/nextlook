import { Orden } from './orden.model';
import { Pago } from './pago.model';

export interface Pedido {
  orden: Orden;
  pago: Pago | null;
}

export type Tono = 'ok' | 'mal' | 'espera' | 'neutro';

/**
 * S11 - Regla UNICA de "esto todavia no se sabe": un pedido se sigue consultando mientras su
 * estado dependa de un evento que aun no llega. Todo lo demas es un estado final o una accion del usuario.
 */
export function enTransito(p: Pedido): boolean {
  if (p.orden.estado === 'PENDIENTE_STOCK') return true; // espera stock.reservado / stock.rechazado
  if (p.orden.estado !== 'PENDIENTE_PAGO') return false; // CARRITO, PAGADA, CANCELADA: nada que esperar
  // PENDIENTE_PAGO: espera a que pago-ms habilite el cobro, o a que orden-ms reciba pago.aprobado/rechazado
  return p.pago === null || p.pago.estado === 'PENDIENTE' || p.pago.estado === 'APROBADO' || p.pago.estado === 'RECHAZADO';
}

export function puedePagar(p: Pedido): boolean {
  return p.orden.estado === 'PENDIENTE_PAGO' && p.pago?.estado === 'LISTO_PARA_PAGAR';
}

/** "Ya no lo quiero": antes de pagar (o desde el carrito). Despues de pagar ya no se ofrece. */
export function puedeCancelar(p: Pedido): boolean {
  return p.orden.estado === 'CARRITO' || puedePagar(p);
}

/** Etapas de la linea de tiempo: 0 registrado, 1 separado, 2 pagado. -1 = cancelado. */
export function etapa(p: Pedido): number {
  switch (p.orden.estado) {
    case 'CANCELADA':
      return -1;
    case 'PAGADA':
      return 2;
    case 'PENDIENTE_PAGO':
      return 1;
    default:
      return 0;
  }
}

/** Texto honesto para cada combinacion: nunca "compra exitosa" antes de tiempo. */
export function descripcion(p: Pedido): { texto: string; tono: Tono } {
  switch (p.orden.estado) {
    case 'CARRITO':
      return { texto: 'Guardado en tu bolsa: no pudimos validarlo ahora', tono: 'neutro' };
    case 'PENDIENTE_STOCK':
      return { texto: 'Separando tus prendas en almacen…', tono: 'espera' };
    case 'PAGADA':
      return { texto: 'Compra realizada · preparando tu paquete', tono: 'ok' };
    case 'CANCELADA': {
      const motivo = p.orden.observacion ?? '';
      if (motivo.startsWith('SIN_STOCK')) return { texto: 'Cancelado: una talla se agoto', tono: 'mal' };
      if (motivo.startsWith('CANCELADA_POR_CLIENTE')) return { texto: 'Cancelado por ti', tono: 'neutro' };
      return { texto: 'Cancelado: pago rechazado', tono: 'mal' };
    }
    case 'PENDIENTE_PAGO':
      switch (p.pago?.estado) {
        case 'LISTO_PARA_PAGAR':
          return { texto: 'Prendas separadas: listo para pagar', tono: 'neutro' };
        case 'APROBADO':
          return { texto: 'Pago aprobado · confirmando tu pedido…', tono: 'espera' };
        case 'RECHAZADO':
          return { texto: 'Pago rechazado · liberando tus prendas…', tono: 'espera' };
        case 'ANULADO':
          return { texto: 'Cancelando tu pedido…', tono: 'espera' };
        default:
          return { texto: 'Preparando el cobro…', tono: 'espera' };
      }
  }
}
