export interface Categoria {
  id: number;
  nombre: string;
}

export interface Variante {
  id: number;
  talla: string;
  color: string;
  sku: string;
  activo: boolean;
}

export type Genero = 'HOMBRE' | 'MUJER' | 'UNISEX';

export interface Prenda {
  id: number;
  nombre: string;
  descripcion: string | null;
  marca: string | null;
  genero: Genero;
  precio: number;
  activo: boolean;
  categoria: Categoria;
  variantes: Variante[];
  /** Ruta relativa al Gateway; usar urlImagen() para mostrarla. */
  imagenUrl: string | null;
}

/** Cuerpo de POST /api/v1/productos (solo ADMIN). */
export interface PrendaRequest {
  nombre: string;
  descripcion: string | null;
  marca: string | null;
  genero: Genero;
  precio: number;
  categoriaId: number;
  variantes: { talla: string; color: string; sku: string }[];
}

/** Stock de inventario-ms (solo ADMIN). */
export interface Stock {
  varianteId: number;
  sku: string;
  disponible: number;
  reservado: number;
}
