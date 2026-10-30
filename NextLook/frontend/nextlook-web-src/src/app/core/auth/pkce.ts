function base64UrlEncode(bytes: Uint8Array): string {
  let binario = '';
  bytes.forEach((byte) => (binario += String.fromCharCode(byte)));
  return btoa(binario).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

/** 32 bytes aleatorios -> 43 caracteres (RFC 7636 exige entre 43 y 128). */
export function generarCodeVerifier(): string {
  return base64UrlEncode(crypto.getRandomValues(new Uint8Array(32)));
}

/** code_challenge = BASE64URL(SHA-256(code_verifier)), metodo S256. */
export async function generarCodeChallenge(verifier: string): Promise<string> {
  const hash = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(verifier));
  return base64UrlEncode(new Uint8Array(hash));
}

/** state: valor de un solo uso contra CSRF en la vuelta desde Keycloak. */
export function generarState(): string {
  return base64UrlEncode(crypto.getRandomValues(new Uint8Array(16)));
}
