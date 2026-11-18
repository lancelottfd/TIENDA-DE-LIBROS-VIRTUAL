import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../config';
import { Auth } from '../services/auth';
import { Notificacion } from '../services/notificacion';
import { describirError } from '../utils/http-error';

/**
 * 1) Envía el JWT (Authorization: Bearer ...) en las solicitudes protegidas.
 * 2) Muestra un aviso con el error HTTP adecuado (400, 401, 403, 404, 409, 5xx).
 * 3) Ante un 401 en una ruta protegida cierra la sesión y lleva al login.
 * Las rutas /auth/** no se tocan: login y registro muestran su propio error.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const notificacion = inject(Notificacion);

  const esApi = req.url.startsWith(API_URL);
  const esAuth = req.url.startsWith(`${API_URL}/auth/`);
  const token = auth.token;

  const peticion =
    esApi && !esAuth && token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(peticion).pipe(
    catchError((err: HttpErrorResponse) => {
      if (esApi && !esAuth) {
        const e = describirError(err);
        notificacion.error(e.titulo, e.mensaje);
        if (err.status === 401) {
          auth.logout();
        }
      }
      return throwError(() => err);
    }),
  );
};
