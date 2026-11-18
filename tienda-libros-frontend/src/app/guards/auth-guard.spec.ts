import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthResponse } from '../models/auth.model';
import { adminGuard, authGuard } from './auth-guard';

const CLAVE_SESION = 'tienda-libros-sesion';

function jwtFalso(): string {
  const b64 = (o: object) =>
    btoa(JSON.stringify(o)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  const exp = Math.floor(Date.now() / 1000) + 3600;
  return `${b64({ alg: 'HS256' })}.${b64({ sub: 'x', exp })}.firma`;
}

function guardarSesion(rol: 'ADMIN' | 'CLIENTE') {
  const datos: AuthResponse = { token: jwtFalso(), tipo: 'Bearer', username: 'x', rol };
  localStorage.setItem(CLAVE_SESION, JSON.stringify(datos));
}

describe('guards de rutas', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient()] });
  });

  afterEach(() => localStorage.clear());

  const ejecutar = (guard: typeof authGuard, url: string) =>
    TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
    );

  it('authGuard: sin sesión redirige a /login recordando la ruta pedida', () => {
    const resultado = ejecutar(authGuard, '/libros/nuevo');

    expect(resultado instanceof UrlTree).toBe(true);
    const arbol = resultado as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(arbol).startsWith('/login')).toBe(true);
    expect(arbol.queryParams['returnUrl']).toBe('/libros/nuevo');
  });

  it('authGuard: con sesión deja pasar', () => {
    guardarSesion('CLIENTE');
    expect(ejecutar(authGuard, '/libros')).toBe(true);
  });

  it('adminGuard: un ADMIN puede entrar', () => {
    guardarSesion('ADMIN');
    expect(ejecutar(adminGuard, '/libros/nuevo')).toBe(true);
  });

  it('adminGuard: un CLIENTE es devuelto al catálogo', () => {
    guardarSesion('CLIENTE');

    const resultado = ejecutar(adminGuard, '/libros/nuevo');

    expect(resultado instanceof UrlTree).toBe(true);
    expect(TestBed.inject(Router).serializeUrl(resultado as UrlTree)).toBe('/libros');
  });
});
