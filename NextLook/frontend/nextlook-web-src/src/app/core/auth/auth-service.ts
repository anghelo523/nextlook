import { Injectable, computed, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { generarCodeChallenge, generarCodeVerifier, generarState } from './pkce';
import { decodeJwtPayload } from './jwt';

interface SesionKeycloak {
  accessToken: string;
  idToken: string;
  expiraEn: number;
}

const CLAVE_VERIFIER = 'nextlook_pkce_verifier';
const CLAVE_STATE = 'nextlook_pkce_state';
const CLAVE_DESTINO = 'nextlook_pkce_destino';

/**
 * S11 - Login con Authorization Code + PKCE contra Keycloak (realm nextlook, cliente nextlook-web).
 * - Tokens SOLO en memoria (signal): nunca en localStorage (XSS).
 * - code_verifier y state en sessionStorage solo durante la ida y vuelta a Keycloak, y se borran al usarse.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly sesion = signal<SesionKeycloak | null>(null);
  private readonly payload = computed(() => decodeJwtPayload(this.sesion()?.accessToken ?? ''));

  readonly autenticado = computed(() => {
    const actual = this.sesion();
    return actual !== null && actual.expiraEn > Date.now();
  });
  readonly roles = computed<string[]>(() => this.payload()?.realm_access?.roles ?? []);
  /** Dueno de las ordenes y pagos: es literalmente el "sub" (keycloak_id en cliente-ms). */
  readonly sub = computed<string | null>(() => this.payload()?.sub ?? null);
  readonly nombre = computed<string | null>(
    () => this.payload()?.given_name ?? this.payload()?.preferred_username ?? null,
  );
  /** Dato de negocio distinto de la identidad: viaja por un protocol mapper propio. */
  readonly tallaPreferida = computed<string | null>(() => this.payload()?.talla_preferida ?? null);

  tieneRol(...rolesPermitidos: string[]): boolean {
    return rolesPermitidos.some((rol) => this.roles().includes(rol));
  }

  accessToken(): string | null {
    return this.autenticado() ? this.sesion()!.accessToken : null;
  }

  async iniciarLogin(destino = location.pathname): Promise<void> {
    const verifier = generarCodeVerifier();
    const state = generarState();
    sessionStorage.setItem(CLAVE_VERIFIER, verifier);
    sessionStorage.setItem(CLAVE_STATE, state);
    sessionStorage.setItem(CLAVE_DESTINO, destino);

    const parametros = new URLSearchParams({
      client_id: environment.keycloak.clientId,
      response_type: 'code',
      redirect_uri: environment.keycloak.redirectUri,
      scope: 'openid profile email',
      state,
      code_challenge: await generarCodeChallenge(verifier),
      code_challenge_method: 'S256',
    });
    location.href = `${environment.keycloak.issuer}/protocol/openid-connect/auth?${parametros}`;
  }

  async completarLogin(code: string, stateRecibido: string): Promise<void> {
    const stateEsperado = sessionStorage.getItem(CLAVE_STATE);
    const verifier = sessionStorage.getItem(CLAVE_VERIFIER);
    // Un solo uso: se borran ANTES de comprobar, asi un segundo intento con el mismo code falla.
    sessionStorage.removeItem(CLAVE_STATE);
    sessionStorage.removeItem(CLAVE_VERIFIER);

    if (!verifier || !stateEsperado || stateRecibido !== stateEsperado) {
      throw new Error('state o code_verifier invalido: posible CSRF o pestana vieja. Inicia sesion de nuevo.');
    }

    // fetch y no HttpClient: esta llamada va directo a Keycloak, fuera de los interceptores del Gateway.
    const respuesta = await fetch(`${environment.keycloak.issuer}/protocol/openid-connect/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: environment.keycloak.clientId,
        redirect_uri: environment.keycloak.redirectUri,
        code,
        code_verifier: verifier,
      }),
    });
    if (!respuesta.ok) {
      throw new Error(`Keycloak rechazo el canje del code (HTTP ${respuesta.status})`);
    }
    const datos = await respuesta.json();
    this.sesion.set({
      accessToken: datos.access_token,
      idToken: datos.id_token,
      expiraEn: Date.now() + datos.expires_in * 1000,
    });
  }

  destinoPendiente(): string {
    const destino = sessionStorage.getItem(CLAVE_DESTINO) ?? '/';
    sessionStorage.removeItem(CLAVE_DESTINO);
    return destino;
  }

  cerrarSesion(): void {
    const idToken = this.sesion()?.idToken;
    this.sesion.set(null);
    const parametros = new URLSearchParams({
      client_id: environment.keycloak.clientId,
      post_logout_redirect_uri: environment.keycloak.postLogoutRedirectUri,
      ...(idToken ? { id_token_hint: idToken } : {}),
    });
    location.href = `${environment.keycloak.issuer}/protocol/openid-connect/logout?${parametros}`;
  }
}
