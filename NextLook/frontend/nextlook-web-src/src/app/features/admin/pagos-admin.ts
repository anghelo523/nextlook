import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PagoService } from '../pedidos/pago-service';
import { Pago } from '../pedidos/pago.model';
import { mensajeDeError } from '../../core/http-error';
import { soles } from '../../shared/imagen';

/** Solo ADMIN: rolGuard en el router + GET /api/v1/pagos hasRole("ADMIN") en Gateway y pago-ms. */
@Component({
  selector: 'app-pagos-admin',
  imports: [DatePipe],
  template: `
    <div class="pagina">
      <header class="cabecera-pagina">
        <div>
          <p class="eyebrow">Panel de administracion</p>
          <h1 class="titulo">Pagos</h1>
        </div>
        <button type="button" class="secundario chico" (click)="cargar()">Actualizar</button>
      </header>

      <div class="resumen">
        <div class="dato"><span class="eyebrow">Cobrado</span><strong>{{ soles(cobrado()) }}</strong></div>
        <div class="dato"><span class="eyebrow">Aprobados</span><strong>{{ contar('APROBADO') }}</strong></div>
        <div class="dato"><span class="eyebrow">Rechazados</span><strong>{{ contar('RECHAZADO') }}</strong></div>
        <div class="dato"><span class="eyebrow">Anulados</span><strong>{{ contar('ANULADO') }}</strong></div>
      </div>

      @if (error()) { <p class="aviso mal">{{ error() }}</p> }
      <div class="tabla-scroll">
        <table>
          <thead><tr><th>Pago</th><th>Orden</th><th>Monto</th><th>Metodo</th><th>Estado</th><th>Transaccion</th><th>Motivo</th><th>Actualizado</th></tr></thead>
          <tbody>
            @for (p of pagos(); track p.id) {
              <tr>
                <td>#{{ p.id }}</td><td>#{{ p.ordenId }}</td><td><strong>{{ soles(p.monto) }}</strong></td>
                <td>{{ p.metodoPago }}</td>
                <td><span class="chip {{ tono(p.estado) }}">{{ p.estado }}</span></td>
                <td class="mono">{{ p.idTransaccion ?? '—' }}</td>
                <td class="muted">{{ p.motivo ?? '—' }}</td>
                <td>{{ p.fechaActualizacion | date: 'dd/MM HH:mm:ss' }}</td>
              </tr>
            } @empty {
              <tr><td colspan="8" class="muted">No hay pagos registrados.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: `
    .titulo { font-size: clamp(2.6rem, 6vw, 4.4rem); margin-top: 0.3rem; }
    .resumen { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-bottom: 1.6rem; }
    .dato { background: var(--blanco); border: 1px solid var(--linea); border-radius: var(--radio-lg); padding: 1.1rem 1.2rem; display: flex; flex-direction: column; gap: 0.3rem; }
    .dato strong { font-family: var(--display); font-size: 2rem; font-weight: 400; line-height: 1; }
    .aviso { margin-bottom: 1rem; }
    @media (max-width: 700px) { .resumen { grid-template-columns: 1fr 1fr; } }
  `,
})
export class PagosAdmin implements OnInit {
  private readonly pagosService = inject(PagoService);
  protected readonly pagos = signal<Pago[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly soles = soles;
  protected readonly cobrado = computed(() =>
    this.pagos().filter((p) => p.estado === 'APROBADO').reduce((s, p) => s + p.monto, 0),
  );

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.pagosService.listar().subscribe({
      next: (data) => this.pagos.set(data),
      error: (err) => this.error.set(mensajeDeError(err, 'No se pudo cargar la lista de pagos.')),
    });
  }

  protected contar(estado: string): number {
    return this.pagos().filter((p) => p.estado === estado).length;
  }

  protected tono(estado: string): string {
    return estado === 'APROBADO' ? 'ok' : estado === 'RECHAZADO' ? 'mal' : estado === 'ANULADO' ? 'neutro' : 'espera';
  }
}
