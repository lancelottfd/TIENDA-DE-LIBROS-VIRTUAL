export type RolUsuario = 'ADMIN' | 'CLIENTE';

/** Cuerpo de POST /api/auth/login */
export interface LoginRequest {
  username: string;
  password: string;
}

/** Cuerpo de POST /api/auth/register (el backend siempre crea un CLIENTE). */
export interface RegisterRequest {
  username: string;
  password: string;
}

/** Respuesta de login/registro: contiene el JWT. */
export interface AuthResponse {
  token: string;
  tipo: string; // "Bearer"
  username: string;
  rol: RolUsuario;
}
