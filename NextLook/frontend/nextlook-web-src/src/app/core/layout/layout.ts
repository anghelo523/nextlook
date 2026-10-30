import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth-service';
import { BolsaService } from '../../features/bolsa/bolsa-service';
import { Icono } from '../../shared/icono';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icono],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class Layout {
  protected readonly auth = inject(AuthService);
  protected readonly bolsa = inject(BolsaService);
  protected readonly menuAbierto = signal(false);
  protected readonly anio = new Date().getFullYear();
}
