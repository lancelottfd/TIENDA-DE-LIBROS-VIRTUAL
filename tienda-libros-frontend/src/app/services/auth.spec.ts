import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { API_URL } from '../config';
import { AuthResponse } from '../models/auth.model';
import { Auth } from './auth';

const CLAVE_SESION = 'tienda-libros-sesion';

/** JWT de mentira con la forma real (cabecera.payload.firma) y un "exp" configurable. */
function jwtFalso(expEnSegundos: number): string {
  const b64 = (o: object) =>
    btoa(JSON.stringify(o)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${b64({ alg: 'HS256' })}.${b64({ sub: 'admin', rol: 'ADMIN', exp: expEnSegundos })}.firma`;
}

const enUnaHora = () => Math.floor(Date.now() / 1000) + 3600;
const haceUnaHora = () => Math.floor(Date.now() / 1000) - 3600;

function respuesta(rol: 'ADMIN' | 'CLIENTE', token = jwtFalso(enUnaHora())): AuthResponse {
  return { token, tipo: 'Bearer', username: rol === 'ADMIN' ? 'admin' : 'cliente', rol };
}

describe('Auth (servicio de sesión)', () => {
  let httpMock: HttpTestingController;

  function configurar() {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
    // logout() navega a /login: se espía para no depender de las rutas reales.
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  }

  beforeEach(() => localStorage.clear());

  afterEach(() => {
    httpMock?.verify();
    localStorage.clear();
  });

  it('login() envía POST /auth/login y guarda el JWT en la sesión y en localStorage', () => {
    configurar();
    const auth = TestBed.inject(Auth);
    const datos = respuesta('ADMIN');

    auth.login({ username: 'admin', password: 'admin123' }).subscribe();

    const req = httpMock.expectOne(`${API_URL}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'admin', password: 'admin123' });
    req.flush(datos);

    expect(auth.estaLogueado()).toBe(true);
    expect(auth.token).toBe(datos.token);
    expect(auth.esAdmin()).toBe(true);
    expect(auth.username()).toBe('admin');
    expect(JSON.parse(localStorage.getItem(CLAVE_SESION)!).token).toBe(datos.token);
  });

  it('un CLIENTE que inicia sesión no es admin', () => {
    configurar();
    const auth = TestBed.inject(Auth);

    auth.login({ username: 'cliente', password: 'cliente123' }).subscribe();
    httpMock.expectOne(`${API_URL}/auth/login`).flush(respuesta('CLIENTE'));

    expect(auth.estaLogueado()).toBe(true);
    expect(auth.esAdmin()).toBe(false);
  });

  it('register() guarda la sesión del nuevo usuario', () => {
    configurar();
    const auth = TestBed.inject(Auth);

    auth.register({ username: 'maria', password: 'secreto123' }).subscribe();
    const req = httpMock.expectOne(`${API_URL}/auth/register`);
    expect(req.request.method).toBe('POST');
    req.flush(respuesta('CLIENTE'));

    expect(auth.estaLogueado()).toBe(true);
    expect(auth.sesion()?.rol).toBe('CLIENTE');
  });

  it('logout() borra la sesión y el localStorage', () => {
    configurar();
    const auth = TestBed.inject(Auth);
    auth.login({ username: 'admin', password: 'admin123' }).subscribe();
    httpMock.expectOne(`${API_URL}/auth/login`).flush(respuesta('ADMIN'));

    auth.logout(false);

    expect(auth.estaLogueado()).toBe(false);
    expect(auth.token).toBeNull();
    expect(localStorage.getItem(CLAVE_SESION)).toBeNull();
  });

  it('restaura la sesión guardada si el token todavía no expiró', () => {
    const datos = respuesta('ADMIN');
    localStorage.setItem(CLAVE_SESION, JSON.stringify(datos));
    configurar();

    const auth = TestBed.inject(Auth);

    expect(auth.estaLogueado()).toBe(true);
    expect(auth.token).toBe(datos.token);
  });

  it('descarta la sesión guardada si el token ya expiró', () => {
    localStorage.setItem(CLAVE_SESION, JSON.stringify(respuesta('ADMIN', jwtFalso(haceUnaHora()))));
    configurar();

    const auth = TestBed.inject(Auth);

    expect(auth.estaLogueado()).toBe(false);
    expect(auth.token).toBeNull();
  });
});
