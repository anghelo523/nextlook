import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { OrdenService } from '../pedidos/orden-service';
import { PagoService } from '../pedidos/pago-service';
import { Orden } from '../pedidos/orden.model';
import { MetodoPago, Pago } from '../pedidos/pago.model';
import { CatalogoService } from '../catalogo/catalogo-service';
import { mensajeDeError } from '../../core/http-error';
import { Icono } from '../../shared/icono';
import { QrDemo } from '../../shared/qr-demo';
import { ConfirmarDialogo } from '../../shared/confirmar-dialogo';
import { soles, urlImagen } from '../../shared/imagen';

/**
 * Pantalla de pago. Es una maquina de estados que sigue a la Saga:
 *  preparando  -> la orden aun espera stock.reservado (o pago-ms aun no habilito el cobro)
 *  elegir      -> el comprador elige Tarjeta, Yape, Plin o "ya no lo quiero"
 *  pasos       -> guia paso a paso del metodo elegido
 *  confirmando -> la pasarela ya respondio; se espera pago.aprobado/rechazado en orden-ms
 *  exito | rechazado | cancelado | no-disponible -> finales
 */
type Fase = 'cargando' | 'preparando' | 'elegir' | 'pasos' | 'confirmando' | 'exito' | 'rechazado' | 'cancelado' | 'no-disponible';

interface MetodoInfo {
  id: MetodoPago;
  nombre: string;
  bajada: string;
  icono: string;
  pasos: string[];
}

const METODOS: MetodoInfo[] = [
  { id: 'TARJETA', nombre: 'Tarjeta', bajada: 'Credito o debito · Visa, Mastercard, Amex', icono: 'tarjeta', pasos: ['Datos de la tarjeta', 'Revisa y paga'] },
  { id: 'YAPE', nombre: 'Yape', bajada: 'Escanea el QR desde tu app Yape', icono: 'qr', pasos: ['Escanea el QR', 'Confirma tu Yape'] },
  { id: 'PLIN', nombre: 'Plin', bajada: 'Desde la app de tu banco', icono: 'celular', pasos: ['Escanea con tu banco', 'Confirma tu Plin'] },
];

@Component({
  selector: 'app-pagar',
  imports: [FormsModule, RouterLink, Icono, QrDemo, ConfirmarDialogo],
  templateUrl: './pagar.html',
  styleUrl: './pagar.css',
})
export class Pagar implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly ordenes = inject(OrdenService);
  private readonly pagos = inject(PagoService);
  private readonly catalogo = inject(CatalogoService);

  protected readonly metodos = METODOS;
  protected readonly soles = soles;
  protected readonly urlImagen = urlImagen;

  protected readonly fase = signal<Fase>('cargando');
  protected readonly orden = signal<Orden | null>(null);
  protected readonly pago = signal<Pago | null>(null);
  protected readonly fotos = signal<Map<number, string | null>>(new Map());
  protected readonly metodo = signal<MetodoInfo | null>(null);
  protected readonly paso = signal(0);
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly preguntarCancelar = signal(false);
  protected readonly tardando = signal(false);

  // datos del formulario (no salen del navegador salvo el titular: la pasarela es simulada)
  protected tarjeta = { numero: '', titular: '', vence: '', cvv: '' };
  protected billetera = { celular: '', codigo: '', titular: '' };

  protected readonly pasosVisibles = computed(() => [...(this.metodo()?.pasos ?? []), 'Listo']);
  protected readonly ultimos4 = computed(() => this.soloDigitos(this.tarjeta.numero).slice(-4));

  private ordenId = 0;
  private temporizador?: ReturnType<typeof setTimeout>;
  private inicioEspera = 0;

  ngOnInit(): void {
    this.ordenId = Number(this.route.snapshot.paramMap.get('id'));
    this.catalogo.fotosPorVariante().subscribe({ next: (f) => this.fotos.set(f), error: () => {} });
    this.consultar();
  }

  ngOnDestroy(): void {
    clearTimeout(this.temporizador);
  }

  // ---------------- seguimiento de la Saga ----------------

  private consultar(): void {
    forkJoin({ orden: this.ordenes.obtener(this.ordenId), pago: this.pagos.deOrden(this.ordenId) }).subscribe({
      next: ({ orden, pago }) => {
        this.orden.set(orden);
        this.pago.set(pago);
        this.decidirFase(orden, pago);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err, 'No pudimos cargar tu pedido.'));
        if (this.fase() === 'cargando') this.fase.set('no-disponible');
        else this.programar(3000);
      },
    });
  }

  private decidirFase(orden: Orden, pago: Pago | null): void {
    const fase = this.fase();
    if (orden.estado === 'PAGADA') return this.terminar('exito');
    if (orden.estado === 'CANCELADA') {
      const motivo = orden.observacion ?? '';
      if (motivo.startsWith('PAGO_RECHAZADO')) return this.terminar('rechazado');
      if (motivo.startsWith('CANCELADA_POR_CLIENTE')) return this.terminar('cancelado');
      return this.terminar('no-disponible'); // SIN_STOCK: una talla se agoto
    }
    if (orden.estado === 'CARRITO') return this.terminar('no-disponible');

    // la pasarela ya respondio: orden-ms se entera por pago.aprobado / pago.rechazado
    if (pago?.estado === 'APROBADO' || pago?.estado === 'RECHAZADO') {
      this.fase.set('confirmando');
      return this.esperar(1500);
    }
    if (orden.estado === 'PENDIENTE_PAGO' && pago?.estado === 'LISTO_PARA_PAGAR') {
      if (fase === 'cargando' || fase === 'preparando') this.fase.set('elegir');
      return;
    }
    // PENDIENTE_STOCK, o pago-ms aun no registro / habilito el cobro
    if (fase === 'cargando') this.inicioEspera = Date.now();
    this.fase.set('preparando');
    this.esperar(2000);
  }

  private esperar(ms: number): void {
    this.tardando.set(Date.now() - this.inicioEspera > 20000);
    this.programar(ms);
  }

  private programar(ms: number): void {
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.consultar(), ms);
  }

  private terminar(fase: Fase): void {
    clearTimeout(this.temporizador);
    this.fase.set(fase);
  }

  // ---------------- eleccion y pasos ----------------

  elegir(m: MetodoInfo): void {
    this.metodo.set(m);
    this.paso.set(0);
    this.error.set(null);
    this.fase.set('pasos');
  }

  volverAElegir(): void {
    this.metodo.set(null);
    this.error.set(null);
    this.fase.set('elegir');
  }

  siguiente(): void {
    this.error.set(null);
    const problema = this.validarPaso();
    if (problema) {
      this.error.set(problema);
      return;
    }
    if (this.paso() < this.pasosVisibles().length - 2) {
      this.paso.update((p) => p + 1);
    } else {
      this.pagar();
    }
  }

  atras(): void {
    this.error.set(null);
    if (this.paso() === 0) this.volverAElegir();
    else this.paso.update((p) => p - 1);
  }

  private validarPaso(): string | null {
    const m = this.metodo()?.id;
    const paso = this.paso();
    if (m === 'TARJETA' && paso === 0) {
      const numero = this.soloDigitos(this.tarjeta.numero);
      if (numero.length < 15 || numero.length > 16) return 'El numero de tarjeta debe tener 15 o 16 digitos.';
      if (!this.tarjeta.titular.trim()) return 'Escribe el nombre del titular tal como aparece en la tarjeta.';
      const vence = /^(\d{2})\/(\d{2})$/.exec(this.tarjeta.vence);
      if (!vence || +vence[1] < 1 || +vence[1] > 12) return 'La fecha de vencimiento debe tener el formato MM/AA.';
      const hoy = new Date();
      const fin = new Date(2000 + +vence[2], +vence[1], 0, 23, 59);
      if (fin < hoy) return 'La tarjeta esta vencida.';
      if (!/^\d{3,4}$/.test(this.tarjeta.cvv)) return 'El CVV tiene 3 o 4 digitos.';
    }
    if ((m === 'YAPE' || m === 'PLIN') && paso === 1) {
      if (!/^9\d{8}$/.test(this.soloDigitos(this.billetera.celular))) return 'El celular debe tener 9 digitos y empezar con 9.';
      if (m === 'YAPE' && !/^\d{6}$/.test(this.billetera.codigo)) return 'El codigo de aprobacion de Yape tiene 6 digitos.';
      if (!this.billetera.titular.trim()) return 'Escribe el nombre del titular de la cuenta.';
    }
    return null;
  }

  private pagar(): void {
    const m = this.metodo();
    if (!m || this.enviando()) return; // un pago a la vez: sin doble clic
    const titular = (m.id === 'TARJETA' ? this.tarjeta.titular : this.billetera.titular).trim();
    this.enviando.set(true);
    this.pagos.procesar(this.ordenId, titular, m.id).subscribe({
      next: (pago) => {
        this.enviando.set(false);
        this.pago.set(pago);
        this.paso.set(this.pasosVisibles().length - 1);
        this.inicioEspera = Date.now();
        // El pago ya tiene resultado, pero la ORDEN todavia no: llega por pago.aprobado/rechazado.
        this.fase.set('confirmando');
        this.esperar(800);
      },
      error: (err) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(err, 'No se pudo procesar el pago. Intenta de nuevo.'));
      },
    });
  }

  // ---------------- "ya no lo quiero" ----------------

  confirmarCancelacion(): void {
    if (this.enviando()) return;
    this.enviando.set(true);
    this.ordenes.cancelar(this.ordenId).subscribe({
      next: (orden) => {
        this.enviando.set(false);
        this.preguntarCancelar.set(false);
        this.orden.set(orden);
        this.terminar('cancelado');
      },
      error: (err) => {
        this.enviando.set(false);
        this.preguntarCancelar.set(false);
        this.error.set(mensajeDeError(err, 'No se pudo cancelar el pedido.'));
      },
    });
  }

  // ---------------- formato de campos ----------------

  formatearNumero(valor: string): void {
    this.tarjeta.numero = this.soloDigitos(valor).slice(0, 16).replace(/(\d{4})(?=\d)/g, '$1 ');
  }

  formatearVence(valor: string): void {
    const d = this.soloDigitos(valor).slice(0, 4);
    this.tarjeta.vence = d.length > 2 ? `${d.slice(0, 2)}/${d.slice(2)}` : d;
  }

  protected soloDigitos(v: string): string {
    return (v ?? '').replace(/\D/g, '');
  }

  protected marcaTarjeta(): string {
    const n = this.soloDigitos(this.tarjeta.numero);
    if (/^4/.test(n)) return 'VISA';
    if (/^(5[1-5]|2[2-7])/.test(n)) return 'MASTERCARD';
    if (/^3[47]/.test(n)) return 'AMEX';
    return 'TARJETA';
  }

  protected nombreMetodo(id: string | undefined): string {
    return METODOS.find((m) => m.id === id)?.nombre ?? id ?? '';
  }
}
