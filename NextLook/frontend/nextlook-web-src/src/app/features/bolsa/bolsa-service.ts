import { Injectable, computed, effect, signal } from '@angular/core';
import { Prenda, Variante } from '../catalogo/prenda.model';

export interface ItemBolsa {
  varianteId: number;
  nombre: string;
  marca: string | null;
  talla: string;
  color: string;
  precio: number;
  cantidad: number;
  imagenUrl: string | null;
}

const CLAVE = 'nextlook_bolsa';

/**
 * La bolsa vive en un signal y se copia en sessionStorage (solo prendas elegidas, nunca tokens),
 * asi sobrevive a recargar la pagina. El precio que se ve aqui es solo referencial:
 * el precio real lo copia orden-ms desde catalogo-ms al crear la orden (S06).
 */
@Injectable({ providedIn: 'root' })
export class BolsaService {
  readonly items = signal<ItemBolsa[]>(leer());
  readonly unidades = computed(() => this.items().reduce((s, i) => s + i.cantidad, 0));
  readonly total = computed(() => this.items().reduce((s, i) => s + i.precio * i.cantidad, 0));

  constructor() {
    effect(() => {
      try {
        sessionStorage.setItem(CLAVE, JSON.stringify(this.items()));
      } catch {
        /* almacenamiento bloqueado: la bolsa sigue funcionando en memoria */
      }
    });
  }

  agregar(prenda: Prenda, variante: Variante): void {
    const existe = this.items().some((i) => i.varianteId === variante.id);
    this.items.update((lista) =>
      existe
        ? lista.map((i) => (i.varianteId === variante.id ? { ...i, cantidad: Math.min(i.cantidad + 1, 10) } : i))
        : [
            ...lista,
            {
              varianteId: variante.id,
              nombre: prenda.nombre,
              marca: prenda.marca,
              talla: variante.talla,
              color: variante.color,
              precio: prenda.precio,
              cantidad: 1,
              imagenUrl: prenda.imagenUrl,
            },
          ],
    );
  }

  cambiarCantidad(varianteId: number, cantidad: number): void {
    const valor = Math.max(1, Math.min(10, Math.floor(cantidad || 1)));
    this.items.update((lista) => lista.map((i) => (i.varianteId === varianteId ? { ...i, cantidad: valor } : i)));
  }

  quitar(varianteId: number): void {
    this.items.update((lista) => lista.filter((i) => i.varianteId !== varianteId));
  }

  vaciar(): void {
    this.items.set([]);
  }
}

function leer(): ItemBolsa[] {
  try {
    const guardado = JSON.parse(sessionStorage.getItem(CLAVE) ?? '[]');
    return Array.isArray(guardado) ? guardado : [];
  } catch {
    return [];
  }
}
