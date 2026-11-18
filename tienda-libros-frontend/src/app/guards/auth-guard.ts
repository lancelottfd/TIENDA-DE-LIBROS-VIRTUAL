import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';
import { Notificacion } from '../services/notificacion';

/** Solo deja pasar a usuarios con sesión; si no, redirige a /login recordando la ruta pedida. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(Auth);
  const router = inject(Router);

  return auth.estaLogueado()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/** Solo para el rol ADMIN (crear/editar libros). Un CLIENTE vuelve al catálogo. */
export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(Auth);
  const router = inject(Router);
  const notificacion = inject(Notificacion);

  if (!auth.estaLogueado()) {
    return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  }
  if (auth.esAdmin()) {
    return true;
  }

  notificacion.error('Acceso restringido', 'Solo un ADMIN puede crear o editar libros.');
  return router.createUrlTree(['/libros']);
};
