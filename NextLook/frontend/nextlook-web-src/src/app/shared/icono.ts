import { Component, computed, input } from '@angular/core';

/** Iconos de trazo (24x24) dibujados aqui mismo: sin librerias ni peticiones extra. */
const TRAZOS: Record<string, string> = {
  bolsa: 'M6 7h12l1 14H5L6 7Z M9 7V6a3 3 0 0 1 6 0v1',
  usuario: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6',
  flecha: 'M5 12h14 M13 6l6 6-6 6',
  atras: 'M19 12H5 M11 6l-6 6 6 6',
  check: 'M5 12.5l4.5 4.5L19 7',
  cerrar: 'M6 6l12 12 M18 6 6 18',
  tarjeta: 'M3 6h18v12H3z M3 10h18 M7 15h3',
  celular: 'M8 3h8a1 1 0 0 1 1 1v16a1 1 0 0 1-1 1H8a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z M11 18h2',
  qr: 'M4 4h6v6H4z M14 4h6v6h-6z M4 14h6v6H4z M14 14h2v2h-2z M18 14h2v2 M14 18h2v2 M18 18h2v2h-2',
  camion: 'M3 7h11v9H3z M14 10h4l3 3v3h-7 M7 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z M17 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z',
  escudo: 'M12 3l8 3v6c0 4.5-3.5 8-8 9-4.5-1-8-4.5-8-9V6l8-3Z M8.5 12l2.5 2.5 4.5-5',
  cambio: 'M4 9h13l-3-3 M20 15H7l3 3',
  basura: 'M5 7h14 M10 7V4h4v3 M7 7l1 13h8l1-13',
  imagen: 'M4 5h16v14H4z M4 16l5-5 4 4 3-3 4 4 M15.5 9.5a1.5 1.5 0 1 0 0-.01',
  mas: 'M12 5v14 M5 12h14',
  reloj: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z M12 7v5l3 2',
  candado: 'M6 11h12v10H6z M8 11V8a4 4 0 0 1 8 0v3',
  x_circulo: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z M9 9l6 6 M15 9l-6 6',
  menu: 'M4 7h16 M4 12h16 M4 17h16',
  salir: 'M15 4h4v16h-4 M10 8l-4 4 4 4 M6 12h10',
};

@Component({
  selector: 'app-icono',
  template: `
    <svg [attr.width]="tam()" [attr.height]="tam()" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         [attr.stroke-width]="grosor()" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path [attr.d]="trazo()" />
    </svg>
  `,
  styles: `:host { display: inline-flex; line-height: 0; }`,
})
export class Icono {
  readonly nombre = input.required<string>();
  readonly tam = input(20);
  readonly grosor = input(1.7);
  protected readonly trazo = computed(() => TRAZOS[this.nombre()] ?? '');
}
