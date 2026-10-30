import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth-service';
import { CatalogoService } from '../../features/catalogo/catalogo-service';
import { Prenda } from '../../features/catalogo/prenda.model';
import { Icono } from '../../shared/icono';
import { soles, urlImagen } from '../../shared/imagen';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink, Icono],
  templateUrl: './inicio.html',
  styleUrl: './inicio.css',
})
export class Inicio implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly catalogo = inject(CatalogoService);

  protected readonly prendas = signal<Prenda[]>([]);
  protected readonly urlImagen = urlImagen;
  protected readonly soles = soles;
  protected readonly anio = new Date().getFullYear();

  /** Lo ultimo que entro al catalogo (ids mas altos). */
  protected readonly novedades = computed(() =>
    [...this.prendas()].filter((p) => p.imagenUrl).sort((a, b) => b.id - a.id).slice(0, 4),
  );

  /** Una tarjeta por categoria, con la foto de su prenda mas reciente. */
  protected readonly categorias = computed(() => {
    const porCategoria = new Map<number, { id: number; nombre: string; foto: string | null; cantidad: number }>();
    for (const p of [...this.prendas()].sort((a, b) => b.id - a.id)) {
      const actual = porCategoria.get(p.categoria.id);
      if (actual) {
        actual.cantidad++;
      } else {
        porCategoria.set(p.categoria.id, { id: p.categoria.id, nombre: p.categoria.nombre, foto: urlImagen(p.imagenUrl), cantidad: 1 });
      }
    }
    return [...porCategoria.values()].sort((a, b) => b.cantidad - a.cantidad).slice(0, 4);
  });

  ngOnInit(): void {
    this.catalogo.todas().subscribe({ next: (p) => this.prendas.set(p.filter((x) => x.activo)) });
  }
}
