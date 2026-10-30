import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { BolsaService } from './bolsa-service';
import { OrdenService } from '../pedidos/orden-service';
import { mensajeDeError } from '../../core/http-error';
import { Icono } from '../../shared/icono';
import { soles, urlImagen } from '../../shared/imagen';

@Component({
  selector: 'app-bolsa',
  imports: [RouterLink, Icono],
  templateUrl: './bolsa.html',
  styleUrl: './bolsa.css',
})
export class Bolsa {
  protected readonly bolsa = inject(BolsaService);
  private readonly ordenes = inject(OrdenService);
  private readonly router = inject(Router);

  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Orden que quedo en CARRITO (HTTP 202, fallback S06): se informa aqui en vez de ir a pagar. */
  protected readonly enCarrito = signal<{ id: number; observacion: string | null } | null>(null);

  protected readonly urlImagen = urlImagen;
  protected readonly soles = soles;

  confirmar(): void {
    if (this.bolsa.items().length === 0 || this.enviando()) return;
    this.enviando.set(true); // evita el doble clic: una sola orden por confirmacion
    this.error.set(null);
    this.ordenes
      .crear({
        // El metodo real se elige en la pantalla de pago; este es solo el valor inicial de la orden.
        metodoPago: 'TARJETA',
        items: this.bolsa.items().map((i) => ({ varianteId: i.varianteId, cantidad: i.cantidad })),
      })
      .subscribe({
        next: (resultado) => {
          this.bolsa.vaciar();
          this.enviando.set(false);
          if (resultado.httpStatus === 202) {
            this.enCarrito.set({ id: resultado.orden.id, observacion: resultado.orden.observacion });
            return;
          }
          // 201: la orden existe y se estan separando las prendas: directo a la pantalla de pago
          this.router.navigate(['/pagar', resultado.orden.id], { state: { traceId: resultado.traceId } });
        },
        error: (err) => {
          this.error.set(mensajeDeError(err, 'No se pudo crear la orden.'));
          this.enviando.set(false);
        },
      });
  }
}
