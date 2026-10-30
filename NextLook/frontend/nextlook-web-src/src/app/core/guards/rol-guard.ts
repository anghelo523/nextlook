import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth-service';

/** Refleja en el router las reglas que YA existen en el Gateway (S07); no las reemplaza. */
export function rolGuard(...rolesPermitidos: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.tieneRol(...rolesPermitidos)) {
      return true;
    }
    alert('Tu cuenta no tiene permiso para esa pantalla.');
    return router.parseUrl('/');
  };
}
