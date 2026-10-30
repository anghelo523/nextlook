import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { rolGuard } from './core/guards/rol-guard';

export const routes: Routes = [
  {
    path: 'auth/callback',
    loadComponent: () => import('./core/auth/auth-callback').then((m) => m.AuthCallback),
  },
  {
    path: '',
    loadComponent: () => import('./core/layout/layout').then((m) => m.Layout),
    children: [
      { path: '', loadComponent: () => import('./core/inicio/inicio').then((m) => m.Inicio) },
      // El catalogo es publico (GET /api/v1/productos es permitAll en el Gateway, S07)
      { path: 'catalogo', loadComponent: () => import('./features/catalogo/catalogo').then((m) => m.Catalogo) },
      {
        path: 'bolsa',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/bolsa/bolsa').then((m) => m.Bolsa),
      },
      {
        path: 'pagar/:id',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/pago/pagar').then((m) => m.Pagar),
      },
      {
        path: 'perfil',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/perfil/perfil').then((m) => m.Perfil),
      },
      {
        path: 'mis-pedidos',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/pedidos/mis-pedidos').then((m) => m.MisPedidos),
      },
      {
        path: 'admin/productos',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () => import('./features/admin/productos-admin').then((m) => m.ProductosAdmin),
      },
      {
        path: 'admin/pagos',
        canActivate: [authGuard, rolGuard('ADMIN')],
        loadComponent: () => import('./features/admin/pagos-admin').then((m) => m.PagosAdmin),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
