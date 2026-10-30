import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth-service';

/** Agrega el Bearer SOLO a las peticiones hacia el Gateway (nunca a terceros). */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }
  const auth = inject(AuthService);
  const token = auth.accessToken();
  const conToken = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(conToken).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401 = sin token o vencido: la unica salida honesta es volver a iniciar sesion.
      // 403 NO se maneja aqui: el token es valido, el rol no alcanza; lo muestra cada pantalla.
      if (error.status === 401) {
        auth.iniciarLogin(location.pathname);
      }
      return throwError(() => error);
    }),
  );
};
