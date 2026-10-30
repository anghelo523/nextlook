export const environment = {
  // Unica direccion de backend que conoce la SPA: el Gateway (S04). Nunca un microservicio directo.
  apiBaseUrl: 'http://localhost:18080',
  keycloak: {
    issuer: 'http://localhost:18443/realms/nextlook',
    clientId: 'nextlook-web', // cliente publico: Authorization Code + PKCE, SIN password grant
    redirectUri: 'http://localhost:4200/auth/callback',
    postLogoutRedirectUri: 'http://localhost:4200/',
  },
  // S11 - estrategia para estados "todavia no se sabe" (ver mis-pedidos.ts)
  seguimiento: {
    intervaloRapidoMs: 3000,
    intervaloLentoMs: 10000,
    pasarALentoTrasMs: 30000,
    rendirseTrasMs: 120000,
  },
};
