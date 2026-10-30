import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from './auth-service';

@Component({
  selector: 'app-auth-callback',
  imports: [RouterLink],
  template: `
    <div class="pantalla">
      <span class="marca">NEXT<span>LOOK</span></span>
      @if (error()) {
        <p class="aviso mal">{{ error() }}</p>
        <a routerLink="/" class="btn claro">Volver al inicio</a>
      } @else {
        <p><span class="giro"></span> Completando inicio de sesion…</p>
      }
    </div>
  `,
  styles: `
    .pantalla {
      min-height: 100dvh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 1.4rem;
      background: var(--tinta); color: #e9e5dc; padding: 1rem; text-align: center;
    }
    .marca { font-family: var(--display); font-size: 3rem; color: white; letter-spacing: 0.04em; }
    .marca span { color: var(--acento); }
    p { display: flex; align-items: center; gap: 0.6rem; margin: 0; }
  `,
})
export class AuthCallback {
  protected readonly error = signal<string | null>(null);

  constructor() {
    const route = inject(ActivatedRoute);
    const router = inject(Router);
    const auth = inject(AuthService);

    const codigo = route.snapshot.queryParamMap.get('code');
    const state = route.snapshot.queryParamMap.get('state');
    const errorKeycloak = route.snapshot.queryParamMap.get('error_description');

    if (errorKeycloak) {
      this.error.set(`Keycloak: ${errorKeycloak}`);
      return;
    }
    if (!codigo || !state) {
      router.navigateByUrl('/');
      return;
    }
    auth
      .completarLogin(codigo, state)
      // replaceUrl: el ?code=...&state=... no queda en el historial del navegador
      .then(() => router.navigateByUrl(auth.destinoPendiente(), { replaceUrl: true }))
      .catch((e: Error) => this.error.set(e.message));
  }
}
