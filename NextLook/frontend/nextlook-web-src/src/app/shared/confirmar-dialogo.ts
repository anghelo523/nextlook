import { Component, ElementRef, afterNextRender, input, output, viewChild } from '@angular/core';

/** Confirmacion modal para acciones que no se deshacen (cancelar un pedido, retirar una prenda). */
@Component({
  selector: 'app-confirmar-dialogo',
  template: `
    <div class="velo" (click)="cancelar.emit()" (keydown.escape)="cancelar.emit()">
      <div class="dialogo" role="alertdialog" aria-modal="true" [attr.aria-labelledby]="'dlg-titulo'"
           [attr.aria-describedby]="'dlg-texto'" (click)="$event.stopPropagation()">
        <h3 id="dlg-titulo">{{ titulo() }}</h3>
        <p id="dlg-texto">{{ texto() }}</p>
        <div class="acciones">
          <button #volver type="button" class="secundario" [disabled]="ocupado()" (click)="cancelar.emit()">{{ textoCancelar() }}</button>
          <button type="button" [class]="peligro() ? 'peligro-lleno' : 'acento'" [disabled]="ocupado()" (click)="confirmar.emit()">
            @if (ocupado()) { <span class="giro"></span> }
            {{ textoConfirmar() }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: `
    .peligro-lleno { background: var(--mal); border-color: var(--mal); }
    .peligro-lleno:hover { background: #a11d23; border-color: #a11d23; }
  `,
})
export class ConfirmarDialogo {
  readonly titulo = input.required<string>();
  readonly texto = input.required<string>();
  readonly textoConfirmar = input('Confirmar');
  readonly textoCancelar = input('Volver');
  readonly peligro = input(false);
  readonly ocupado = input(false);
  readonly confirmar = output<void>();
  readonly cancelar = output<void>();

  private readonly volver = viewChild<ElementRef<HTMLButtonElement>>('volver');

  constructor() {
    // el foco empieza en la opcion segura: un Enter apurado no cancela el pedido
    afterNextRender(() => this.volver()?.nativeElement.focus());
  }
}
