export interface JwtPayload {
  sub: string;
  exp: number;
  azp?: string;
  email?: string;
  given_name?: string;
  preferred_username?: string;
  realm_access?: { roles: string[] };
  /** S11 - dato de negocio propio: user attribute + protocol mapper del realm nextlook. */
  talla_preferida?: string;
}

/** Solo LEE los claims. La firma la valida el Gateway (resource server, S07), nunca la SPA. */
export function decodeJwtPayload(token: string): JwtPayload | null {
  const partes = token.split('.');
  if (partes.length !== 3) return null;
  try {
    const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join(''),
    );
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}
