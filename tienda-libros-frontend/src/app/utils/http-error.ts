import { HttpErrorResponse } from '@angular/common/http';

export interface ErrorDescrito {
  titulo: string;
  mensaje: string;
}

interface CuerpoError {
  mensaje?: string;
  errores?: Record<string, string>;
}

/**
 * Convierte un error HTTP del backend ({"mensaje": "..."}) en un texto para el usuario.
 *
 * @param enFormularioAuth true cuando el error viene de login/registro: ahí un 401
 *        significa "credenciales incorrectas" y no "sesión expirada".
 */
export function describirError(err: unknown, enFormularioAuth = false): ErrorDescrito {
  if (!(err instanceof HttpErrorResponse)) {
    return { titulo: 'Error inesperado', mensaje: 'Ocurrió un error inesperado. Intenta nuevamente.' };
  }

  const cuerpo: CuerpoError | null =
    err.error && typeof err.error === 'object' ? (err.error as CuerpoError) : null;
  const delServidor = typeof cuerpo?.mensaje === 'string' ? cuerpo.mensaje : null;
  const detalle = cuerpo?.errores ? Object.values(cuerpo.errores).join(' · ') : null;

  switch (err.status) {
    case 0:
      return {
        titulo: 'Sin conexión',
        mensaje: 'No se pudo conectar con el servidor. Verifica que el backend esté en ejecución (puerto 8080).',
      };
    case 400:
      return { titulo: 'Datos inválidos', mensaje: detalle ?? delServidor ?? 'La solicitud no es válida.' };
    case 401:
      return enFormularioAuth
        ? { titulo: 'No se pudo iniciar sesión', mensaje: delServidor ?? 'Usuario o contraseña incorrectos.' }
        : { titulo: 'Sesión no válida', mensaje: 'Tu sesión expiró o no estás autenticado. Inicia sesión nuevamente.' };
    case 403:
      return { titulo: 'Sin permisos', mensaje: delServidor ?? 'No tienes permisos para esta operación.' };
    case 404:
      return { titulo: 'No encontrado', mensaje: delServidor ?? 'El recurso solicitado no existe.' };
    case 409:
      return { titulo: 'Conflicto', mensaje: delServidor ?? 'La operación entra en conflicto con datos existentes.' };
    default:
      return err.status >= 500
        ? { titulo: 'Error del servidor', mensaje: delServidor ?? 'Error interno del servidor.' }
        : { titulo: 'Error', mensaje: delServidor ?? 'No se pudo completar la operación.' };
  }
}
