import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { environment } from '../../../environments/environment';
import { OrdenService } from './orden-service';
import { PagoService } from './pago-service';
import { Pedido, descripcion, enTransito, etapa, puedeCancelar, puedePagar } from './seguimiento';
import { CatalogoService } from '../catalogo/catalogo-service';
import { mensajeDeError } from '../../core/http-error';
import { Icono } from '../../shared/icono';
import { ConfirmarDialogo } from '../../shared/confirmar-dialogo';
import { soles, urlImagen } from '../../shared/imagen';

/**
 * S11 - "Mis pedidos" con POLLING ADAPTATIVO:
 *  - solo se consultan los pedidos en transito (enTransito), nunca los finales;
 *  - cada 3 s al inicio; si pasan 30 s sin resolverse, cada 10 s y se avisa que tarda mas de lo normal;
 *  - a los 2 min se deja de preguntar y se ofrece "volver a consultar" (algo en el backend esta caido);
 *  - se pausa con la pestana oculta y se limpia al salir de la pantalla (ngOnDestroy).
 * El cobro en si se hace en la pantalla /pagar/:id (Tarjeta, Yape o Plin, paso a paso).
 */
@Component({
  selector: 'app-mis-pedidos',
  imports: [DatePipe, RouterLink, Icono, ConfirmarDialogo],
  templateUrl: './mis-pedidos.html',
  styleUrl: './mis-pedidos.css',
})
export class MisPedidos implements OnInit, OnDestroy {
  private readonly ordenes = inject(OrdenService);
  private readonly pagos = inject(PagoService);
  private readonly catalogo = inject(CatalogoService);
  private readonly router = inject(Router);
  private readonly cfg = environment.seguimiento;

  protected readonly pedidos = signal<Pedido[]>([]);
  protected readonly fotos = signal<Map<number, string | null>>(new Map());
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly lento = signal(false);
  protected readonly detenido = signal(false);
  protected readonly ultimaConsulta = signal<Date | null>(null);
  protected readonly aCancelar = signal<Pedido | null>(null);
  protected readonly cancelando = signal(false);

  protected readonly descripcion = descripcion;
  protected readonly puedePagar = puedePagar;
  protected readonly puedeCancelar = puedeCancelar;
  protected readonly enTransito = enTransito;
  protected readonly etapa = etapa;
  protected readonly soles = soles;
  protected readonly urlImagen = urlImagen;
  protected readonly etapas = ['Pedido registrado', 'Prendas separadas', 'Compra realizada'];

  private temporizador?: ReturnType<typeof setTimeout>;
  private inicioEspera = Date.now();
  private readonly alCambiarVisibilidad = () => {
    if (document.visibilityState === 'visible') this.programar(0);
  };

  ngOnInit(): void {
    document.addEventListener('visibilitychange', this.alCambiarVisibilidad);
    this.catalogo.fotosPorVariante().subscribe({ next: (f) => this.fotos.set(f), error: () => {} });
    this.ordenes.mias().subscribe({
      next: (lista) => {
        this.pedidos.set(lista.map((orden) => ({ orden, pago: null })));
        this.cargando.set(false);
        // el pago de cada orden se trae una vez; luego solo se refrescan las que estan en transito
        lista.forEach((o) => this.pagos.deOrden(o.id).subscribe((pago) => this.actualizar(o.id, { pago })));
        this.reiniciarEspera();
      },
      error: (err) => {
        this.error.set(mensajeDeError(err, 'No se pudieron cargar tus pedidos.'));
        this.cargando.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    clearTimeout(this.temporizador);
    document.removeEventListener('visibilitychange', this.alCambiarVisibilidad);
  }

  /** Vuelve a empezar el ciclo rapido: tras una accion del usuario o al pulsar "volver a consultar". */
  protected reiniciarEspera(): void {
    this.inicioEspera = Date.now();
    this.lento.set(false);
    this.detenido.set(false);
    this.programar(this.cfg.intervaloRapidoMs);
  }

  private programar(ms: number): void {
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.consultar(), ms);
  }

  private consultar(): void {
    const pendientes = this.pedidos().filter(enTransito);
    if (pendientes.length === 0) return; // nada que esperar: el polling se apaga solo
    if (document.visibilityState === 'hidden') return; // se reanuda en alCambiarVisibilidad

    const esperado = Date.now() - this.inicioEspera;
    if (esperado > this.cfg.rendirseTrasMs) {
      this.detenido.set(true);
      return;
    }
    this.lento.set(esperado > this.cfg.pasarALentoTrasMs);

    for (const p of pendientes) {
      forkJoin({ orden: this.ordenes.obtener(p.orden.id), pago: this.pagos.deOrden(p.orden.id) }).subscribe({
        next: (r) => this.actualizar(p.orden.id, r),
        error: () => {
          /* un fallo puntual no detiene el seguimiento: se reintenta en la proxima vuelta */
        },
      });
    }
    this.ultimaConsulta.set(new Date());
    this.programar(this.lento() ? this.cfg.intervaloLentoMs : this.cfg.intervaloRapidoMs);
  }

  private actualizar(ordenId: number, cambios: Partial<Pedido>): void {
    this.pedidos.update((lista) => lista.map((p) => (p.orden.id === ordenId ? { ...p, ...cambios } : p)));
  }

  pagar(p: Pedido): void {
    this.router.navigate(['/pagar', p.orden.id]);
  }

  confirmarCancelacion(): void {
    const p = this.aCancelar();
    if (!p || this.cancelando()) return;
    this.cancelando.set(true);
    this.ordenes.cancelar(p.orden.id).subscribe({
      next: (orden) => {
        this.actualizar(p.orden.id, { orden });
        this.cancelando.set(false);
        this.aCancelar.set(null);
        this.reiniciarEspera();
      },
      error: (err) => {
        this.error.set(mensajeDeError(err, 'No se pudo cancelar el pedido.'));
        this.cancelando.set(false);
        this.aCancelar.set(null);
      },
    });
  }

  reintentarCarrito(p: Pedido): void {
    this.ordenes.confirmarCarrito(p.orden.id).subscribe({
      next: (orden) => {
        this.actualizar(p.orden.id, { orden });
        this.reiniciarEspera();
      },
      error: (err) => this.error.set(mensajeDeError(err, 'Todavia no se pudo confirmar.')),
    });
  }

  protected nombreMetodo(m: string): string {
    return m === 'TARJETA' ? 'Tarjeta' : m === 'YAPE' ? 'Yape' : m === 'PLIN' ? 'Plin' : m;
  }
}
