export interface Direccion {
  id: number;
  alias: string;
  direccion: string;
  distrito: string;
  ciudad: string;
  referencia: string | null;
  principal: boolean;
}

export interface Cliente {
  id: number;
  nombres: string;
  apellidos: string;
  email: string;
  dni: string | null;
  telefono: string | null;
  direcciones: Direccion[];
  direccionPrincipal: Direccion | null;
}

export interface PerfilRequest {
  nombres: string;
  apellidos: string;
  dni: string | null;
  telefono: string | null;
}

export interface DireccionRequest {
  alias: string;
  direccion: string;
  distrito: string;
  ciudad: string;
  referencia: string | null;
  principal: boolean;
}
