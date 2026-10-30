import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { concat, concatMap, forkJoin, last, of, toArray } from 'rxjs';
import { CatalogoService } from '../catalogo/catalogo-service';
import { Categoria, Genero, Prenda, Stock } from '../catalogo/prenda.model';
import { mensajeDeError } from '../../core/http-error';
import { Icono } from '../../shared/icono';
import { ConfirmarDialogo } from '../../shared/confirmar-dialogo';
import { soles, urlImagen } from '../../shared/imagen';

interface FilaVariante {
  talla: string;
  color: string;
  stock: number;
}

/**
 * Solo ADMIN: rolGuard en el router, y Gateway + catalogo-ms + inventario-ms exigen rol ADMIN para escribir.
 * Alta de una prenda = 3 llamadas: POST /productos -> PUT /productos/{id}/imagen -> PUT /inventario/{varianteId}.
 */
@Component({
  selector: 'app-productos-admin',
  imports: [FormsModule, Icono, ConfirmarDialogo],
  templateUrl: './productos-admin.html',
  styleUrl: './productos-admin.css',
})
export class ProductosAdmin implements OnInit {
  private readonly catalogo = inject(CatalogoService);

  protected readonly prendas = signal<Prenda[]>([]);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly stock = signal<Map<number, Stock>>(new Map());
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);
  protected readonly formAbierto = signal(false);
  protected readonly guardando = signal(false);
  protected readonly subiendoFoto = signal<number | null>(null);
  protected readonly aRetirar = signal<Prenda | null>(null);
  protected readonly busqueda = signal('');

  protected readonly urlImagen = urlImagen;
  protected readonly soles = soles;

  // formulario de alta
  protected form = this.formVacio();
  protected filas: FilaVariante[] = [{ talla: 'M', color: '', stock: 10 }];
  protected readonly foto = signal<Blob | null>(null);
  protected readonly vistaPrevia = signal<string | null>(null);
  protected nuevaCategoria = '';

  protected readonly filtradas = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    const lista = [...this.prendas()].sort((a, b) => b.id - a.id);
    return q ? lista.filter((p) => `${p.nombre} ${p.marca} ${p.categoria.nombre}`.toLowerCase().includes(q)) : lista;
  });

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    forkJoin({ prendas: this.catalogo.prendas(), categorias: this.catalogo.categorias(), stock: this.catalogo.stock() }).subscribe({
      next: ({ prendas, categorias, stock }) => {
        this.prendas.set(prendas);
        this.categorias.set(categorias);
        this.stock.set(new Map(stock.map((s) => [s.varianteId, s])));
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err, 'No se pudo cargar el catalogo.'));
        this.cargando.set(false);
      },
    });
  }

  protected stockTotal(p: Prenda): number {
    return p.variantes.reduce((s, v) => s + (this.stock().get(v.id)?.disponible ?? 0), 0);
  }

  // ---------------- foto ----------------

  /** Se reduce en el navegador (max 1000 px, JPEG): sube rapido y nunca pasa el limite de 3 MB. */
  async elegirFoto(archivo: File | undefined): Promise<void> {
    if (!archivo) return;
    if (!archivo.type.startsWith('image/')) {
      this.error.set('El archivo debe ser una imagen (JPG, PNG o WEBP).');
      return;
    }
    const reducida = await reducir(archivo);
    this.foto.set(reducida);
    const anterior = this.vistaPrevia();
    if (anterior) URL.revokeObjectURL(anterior);
    this.vistaPrevia.set(URL.createObjectURL(reducida));
  }

  async cambiarFoto(prenda: Prenda, archivo: File | undefined): Promise<void> {
    if (!archivo) return;
    this.subiendoFoto.set(prenda.id);
    this.error.set(null);
    const reducida = await reducir(archivo);
    this.catalogo.subirImagen(prenda.id, reducida).subscribe({
      next: (actualizada) => {
        this.prendas.update((lista) => lista.map((p) => (p.id === actualizada.id ? actualizada : p)));
        this.subiendoFoto.set(null);
        this.avisar(`Foto de "${prenda.nombre}" actualizada.`);
      },
      error: (err) => {
        this.subiendoFoto.set(null);
        this.error.set(mensajeDeError(err, 'No se pudo subir la foto.'));
      },
    });
  }

  // ---------------- variantes ----------------

  agregarFila(): void {
    const ultima = this.filas[this.filas.length - 1];
    this.filas = [...this.filas, { talla: '', color: ultima?.color ?? '', stock: ultima?.stock ?? 10 }];
  }

  quitarFila(i: number): void {
    this.filas = this.filas.filter((_, k) => k !== i);
  }

  crearCategoria(): void {
    const nombre = this.nuevaCategoria.trim();
    if (!nombre) return;
    this.catalogo.crearCategoria(nombre).subscribe({
      next: (c) => {
        this.categorias.update((l) => [...l, c]);
        this.form.categoriaId = c.id;
        this.nuevaCategoria = '';
      },
      error: (err) => this.error.set(mensajeDeError(err, 'No se pudo crear la categoria.')),
    });
  }

  // ---------------- alta ----------------

  guardar(): void {
    this.error.set(null);
    const problema = this.validar();
    if (problema) {
      this.error.set(problema);
      return;
    }
    const sufijo = Date.now().toString(36).toUpperCase().slice(-5);
    const variantes = this.filas.map((f) => ({
      talla: f.talla.trim().toUpperCase(),
      color: f.color.trim(),
      sku: `NL-${sufijo}-${f.talla.trim().toUpperCase().slice(0, 4)}-${sinTildes(f.color).slice(0, 3).toUpperCase()}`,
    }));
    this.guardando.set(true);
    this.catalogo
      .crearPrenda({
        nombre: this.form.nombre.trim(),
        descripcion: this.form.descripcion.trim() || null,
        marca: this.form.marca.trim() || null,
        genero: this.form.genero,
        precio: Number(this.form.precio),
        categoriaId: Number(this.form.categoriaId),
        variantes,
      })
      .pipe(
        concatMap((prenda) => {
          const foto = this.foto();
          const conFoto = foto ? this.catalogo.subirImagen(prenda.id, foto) : of(prenda);
          return conFoto.pipe(
            concatMap((p) =>
              concat(
                ...p.variantes.map((v) => {
                  const fila = this.filas[variantes.findIndex((x) => x.sku === v.sku)];
                  return this.catalogo.registrarStock(v.id, v.sku, Math.max(0, Math.floor(fila?.stock ?? 0)));
                }),
              ).pipe(toArray(), concatMap(() => of(p))),
            ),
          );
        }),
        last(),
      )
      .subscribe({
        next: (p) => {
          this.guardando.set(false);
          this.avisar(`"${p.nombre}" ya esta a la venta en el catalogo.`);
          this.reiniciarForm();
          this.formAbierto.set(false);
          this.cargar();
        },
        error: (err) => {
          this.guardando.set(false);
          this.error.set(mensajeDeError(err, 'No se pudo guardar la prenda.'));
          this.cargar(); // si la prenda alcanzo a crearse, que se vea en la lista
        },
      });
  }

  private validar(): string | null {
    if (!this.form.nombre.trim()) return 'Escribe el nombre de la prenda.';
    if (!(Number(this.form.precio) >= 0.1)) return 'El precio debe ser mayor a S/ 0.10.';
    if (!this.form.categoriaId) return 'Elige una categoria.';
    if (this.filas.length === 0) return 'Agrega al menos una talla.';
    const claves = new Set<string>();
    for (const f of this.filas) {
      if (!f.talla.trim() || !f.color.trim()) return 'Cada variante necesita talla y color.';
      if (f.talla.trim().length > 10) return 'La talla admite hasta 10 caracteres.';
      const clave = `${f.talla.trim().toUpperCase()}|${f.color.trim().toLowerCase()}`;
      if (claves.has(clave)) return `La combinacion ${f.talla} / ${f.color} esta repetida.`;
      claves.add(clave);
      if (!(f.stock >= 0)) return 'El stock no puede ser negativo.';
    }
    if (!this.foto()) return 'Sube una foto de referencia de la prenda.';
    return null;
  }

  // ---------------- retirar ----------------

  confirmarRetiro(): void {
    const p = this.aRetirar();
    if (!p) return;
    this.guardando.set(true);
    this.catalogo.retirar(p.id).subscribe({
      next: () => {
        this.guardando.set(false);
        this.aRetirar.set(null);
        this.prendas.update((l) => l.filter((x) => x.id !== p.id));
        this.avisar(`"${p.nombre}" se retiro del catalogo.`);
      },
      error: (err) => {
        this.guardando.set(false);
        this.aRetirar.set(null);
        this.error.set(mensajeDeError(err, 'No se pudo retirar la prenda.'));
      },
    });
  }

  // ---------------- utilidades ----------------

  abrirForm(): void {
    this.reiniciarForm();
    this.formAbierto.set(true);
  }

  private reiniciarForm(): void {
    this.form = this.formVacio();
    this.filas = [{ talla: 'M', color: '', stock: 10 }];
    this.foto.set(null);
    const anterior = this.vistaPrevia();
    if (anterior) URL.revokeObjectURL(anterior);
    this.vistaPrevia.set(null);
  }

  private formVacio() {
    return { nombre: '', descripcion: '', marca: '', genero: 'UNISEX' as Genero, precio: null as number | null, categoriaId: null as number | null };
  }

  private avisar(texto: string): void {
    this.exito.set(texto);
    setTimeout(() => this.exito.set(null), 4000);
  }
}

function sinTildes(texto: string): string {
  return texto.normalize('NFD').replace(/[^A-Za-z]/g, '');
}

/** Redimensiona a un maximo de 1000 px de lado y la convierte a JPEG (calidad 0.85). */
async function reducir(archivo: File): Promise<Blob> {
  const bitmap = await createImageBitmap(archivo);
  const escala = Math.min(1, 1000 / Math.max(bitmap.width, bitmap.height));
  const lienzo = document.createElement('canvas');
  lienzo.width = Math.round(bitmap.width * escala);
  lienzo.height = Math.round(bitmap.height * escala);
  const ctx = lienzo.getContext('2d')!;
  ctx.fillStyle = '#ffffff'; // un PNG con transparencia queda sobre blanco, no sobre negro
  ctx.fillRect(0, 0, lienzo.width, lienzo.height);
  ctx.drawImage(bitmap, 0, 0, lienzo.width, lienzo.height);
  bitmap.close();
  return new Promise((resolver, rechazar) =>
    lienzo.toBlob((b) => (b ? resolver(b) : rechazar(new Error('No se pudo procesar la imagen'))), 'image/jpeg', 0.85),
  );
}
