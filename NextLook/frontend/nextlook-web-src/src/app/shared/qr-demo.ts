import { Component, computed, input } from '@angular/core';

/**
 * QR ILUSTRATIVO para la pasarela simulada: tiene la forma de un QR (tres marcas de esquina y modulos)
 * pero no codifica nada real. Se genera igual para el mismo texto, asi cada orden tiene "su" QR.
 */
@Component({
  selector: 'app-qr-demo',
  template: `
    <svg [attr.viewBox]="'0 0 ' + (N + 2) + ' ' + (N + 2)" role="img" [attr.aria-label]="'Codigo QR de prueba para ' + texto()"
         shape-rendering="crispEdges">
      <rect width="100%" height="100%" fill="#fff" />
      @for (m of modulos(); track $index) {
        <rect [attr.x]="m[0] + 1" [attr.y]="m[1] + 1" width="1" height="1" fill="#0e0e0e" />
      }
    </svg>
  `,
  styles: `:host { display: block; } svg { width: 100%; height: auto; display: block; }`,
})
export class QrDemo {
  readonly texto = input.required<string>();
  protected readonly N = 25;

  protected readonly modulos = computed(() => {
    const n = this.N;
    let semilla = [...this.texto()].reduce((h, c) => (h * 31 + c.charCodeAt(0)) >>> 0, 2166136261);
    const azar = () => ((semilla = (semilla * 1664525 + 1013904223) >>> 0) / 2 ** 32);
    const enMarca = (x: number, y: number) =>
      (x < 8 && y < 8) || (x >= n - 8 && y < 8) || (x < 8 && y >= n - 8);
    const celdas: [number, number][] = [];
    for (let y = 0; y < n; y++) {
      for (let x = 0; x < n; x++) {
        if (enMarca(x, y)) {
          // marca de esquina: cuadrado 7x7 con borde, hueco y centro 3x3
          const bx = x < 8 ? x : x - (n - 7);
          const by = y < 8 ? y : y - (n - 7);
          if (bx > 6 || by > 6 || bx < 0 || by < 0) continue;
          const borde = bx === 0 || by === 0 || bx === 6 || by === 6;
          const centro = bx >= 2 && bx <= 4 && by >= 2 && by <= 4;
          if (borde || centro) celdas.push([x, y]);
        } else if (azar() < 0.48) {
          celdas.push([x, y]);
        }
      }
    }
    return celdas;
  });
}
