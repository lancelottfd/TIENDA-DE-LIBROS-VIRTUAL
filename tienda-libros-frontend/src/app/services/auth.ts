import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { API_URL } from '../config';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';

const CLAVE_SESION = 'tienda-libros-sesion';

/** Sesión del usuario: guarda el JWT y expone el estado como signals. */
@Injectable({ providedIn: 'root' })
export class Auth {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly _sesion = signal<AuthResponse | null>(this.leerSesion());

  readonly sesion = this._sesion.asReadonly();
  readonly estaLogueado = computed(() => this._sesion() !== null);
  readonly esAdmin = computed(() => this._sesion()?.rol === 'ADMIN');
  readonly username = computed(() => this._sesion()?.username ?? '');

  get token(): string | null {
    return this._sesion()?.token ?? null;
  }

  login(datos: LoginRequest) {
    return this.http
      .post<AuthResponse>(`${API_URL}/auth/login`, datos)
      .pipe(tap((resp) => this.guardarSesion(resp)));
  }

  register(datos: RegisterRequest) {
    return this.http
      .post<AuthResponse>(`${API_URL}/auth/register`, datos)
      .pipe(tap((resp) => this.guardarSesion(resp)));
  }

  logout(redirigir = true) {
    this._sesion.set(null);
    try {
      localStorage.removeItem(CLAVE_SESION);
    } catch {
      /* almacenamiento no disponible */
    }
    if (redirigir) {
      this.router.navigate(['/login']);
    }
  }

  private guardarSesion(resp: AuthResponse) {
    this._sesion.set(resp);
    try {
      localStorage.setItem(CLAVE_SESION, JSON.stringify(resp));
    } catch {
      /* almacenamiento no disponible */
    }
  }

  private leerSesion(): AuthResponse | null {
    try {
      const crudo = localStorage.getItem(CLAVE_SESION);
      if (!crudo) return null;
      const sesion = JSON.parse(crudo) as AuthResponse;
      return sesion?.token && !this.tokenExpirado(sesion.token) ? sesion : null;
    } catch {
      return null;
    }
  }

  /** Lee el campo "exp" del JWT (sin verificar la firma: eso lo hace el backend). */
  private tokenExpirado(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return typeof payload.exp === 'number' && payload.exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }
}

