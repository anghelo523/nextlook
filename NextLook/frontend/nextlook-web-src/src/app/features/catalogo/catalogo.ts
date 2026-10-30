import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CatalogoService } from './catalogo-service';
import { Categoria, Genero, Prenda, Variante } from './prenda.model';
import { BolsaService } from '../bolsa/bolsa-service';
import { AuthService } from '../../core/auth/auth-service';
import { mensajeDeError } from '../../core/http-error';
import { Icono } from '../../shared/icono';
import { soles, urlImagen } from '../../shared/imagen';

type Orden = 'nuevo' | 'precio-asc' | 'precio-desc';

/** Lo que el comprador eligio en cada tarjeta: primero el color, luego la talla. */
interface Eleccion {
  color: string;
  varianteId: number;
}

@Component({
  selector: 'app-catalogo',
  imports: [Icono],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css',
})
export class Catalogo implements OnInit {
  private readonly catalogo = inject(CatalogoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  protected readonly bolsa = inject(BolsaService);
  protected readonly auth = inject(AuthService);

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly prendas = signal<Prenda[]>([]);
  protected readonly categoriaActual = signal<number | null>(null);
  protected readonly generoActual = signal<Genero | null>(null);
  protected readonly orden = signal<Orden>('nuevo');
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly eleccion = signal<Record<number, Eleccion>>({});
  protected readonly agregado = signal<number | null>(null);

  protected readonly urlImagen = urlImagen;
  protected readonly soles = soles;
  protected readonly generos: { valor: Genero | null; texto: string }[] = [
    { valor: null, texto: 'Todos' },
    { valor: 'MUJER', texto: 'Mujer' },
    { valor: 'HOMBRE', texto: 'Hombre' },
    { valor: 'UNISEX', texto: 'Unisex' },
  ];

  protected readonly visibles = computed(() => {
    const genero = this.generoActual();
    const lista = this.prendas().filter((p) => !genero || p.genero === genero || (genero !== 'UNISEX' && p.genero === 'UNISEX'));
    switch (this.orden()) {
      case 'precio-asc':
        return [...lista].sort((a, b) => a.precio - b.precio);
      case 'precio-desc':
        return [...lista].sort((a, b) => b.precio - a.precio);
      default:
        return [...lista].sort((a, b) => b.id - a.id);
    }
  });

  protected readonly tituloCategoria = computed(
    () => this.categorias().find((c) => c.id === this.categoriaActual())?.nombre ?? 'Todo el catalogo',
  );

  ngOnInit(): void {
    this.catalogo.categorias().subscribe({ next: (c) => this.categorias.set(c) });
    // Los filtros viven en la URL: los enlaces de la portada y el boton "atras" funcionan solos
    this.route.queryParamMap.subscribe((q) => {
      const categoria = Number(q.get('categoria')) || null;
      const genero = q.get('genero') as Genero | null;
      this.generoActual.set(genero && ['HOMBRE', 'MUJER', 'UNISEX'].includes(genero) ? genero : null);
      if (categoria !== this.categoriaActual() || this.cargando()) {
        this.categoriaActual.set(categoria);
        this.cargar();
      }
    });
  }

  filtrar(cambios: { categoria?: number | null; genero?: Genero | null }): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: cambios,
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  private cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.catalogo.prendas(this.categoriaActual() ?? undefined).subscribe({
      next: (data) => {
        const activas = data.filter((p) => p.activo && p.variantes.some((v) => v.activo));
        this.prendas.set(activas);
        this.eleccion.set(Object.fromEntries(activas.map((p) => [p.id, this.eleccionInicial(p)])));
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err, 'No se pudo cargar el catalogo.'));
        this.cargando.set(false);
      },
    });
  }

  protected colores(prenda: Prenda): string[] {
    return [...new Set(prenda.variantes.filter((v) => v.activo).map((v) => v.color))];
  }

  protected tallas(prenda: Prenda): Variante[] {
    const color = this.eleccion()[prenda.id]?.color;
    return prenda.variantes.filter((v) => v.activo && v.color === color);
  }

  /** S11 - arranca en la talla_preferida del token (protocol mapper), si la prenda la tiene. */
  private eleccionInicial(prenda: Prenda): Eleccion {
    const activas = prenda.variantes.filter((v) => v.activo);
    const talla = this.auth.tallaPreferida();
    const v = activas.find((x) => x.talla === talla) ?? activas[0];
    return { color: v.color, varianteId: v.id };
  }

  elegirColor(prenda: Prenda, color: string): void {
    const actual = this.eleccion()[prenda.id];
    const tallaActual = prenda.variantes.find((v) => v.id === actual?.varianteId)?.talla;
    const opciones = prenda.variantes.filter((v) => v.activo && v.color === color);
    const v = opciones.find((x) => x.talla === tallaActual) ?? opciones[0];
    this.eleccion.update((e) => ({ ...e, [prenda.id]: { color, varianteId: v.id } }));
  }

  elegirTalla(prenda: Prenda, variante: Variante): void {
    this.eleccion.update((e) => ({ ...e, [prenda.id]: { color: variante.color, varianteId: variante.id } }));
  }

  agregar(prenda: Prenda): void {
    const variante = prenda.variantes.find((v) => v.id === this.eleccion()[prenda.id]?.varianteId);
    if (!variante) return;
    this.bolsa.agregar(prenda, variante);
    this.agregado.set(prenda.id);
    setTimeout(() => this.agregado.set(null), 1600);
  }

  protected generoTexto(g: Genero): string {
    return g === 'MUJER' ? 'Mujer' : g === 'HOMBRE' ? 'Hombre' : 'Unisex';
  }
}
