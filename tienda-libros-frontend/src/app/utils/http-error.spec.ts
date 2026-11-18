import { HttpErrorResponse } from '@angular/common/http';
import { describirError } from './http-error';

const error = (status: number, cuerpo?: unknown) =>
  new HttpErrorResponse({ status, error: cuerpo ?? null });

describe('describirError', () => {
  it('status 0: avisa que no hay conexión con el backend', () => {
    expect(describirError(error(0)).titulo).toBe('Sin conexión');
  });

  it('400 con detalle por campo: concatena los mensajes de validación', () => {
    const e = describirError(error(400, { mensaje: 'Hay datos inválidos', errores: { titulo: 'El título es obligatorio' } }));
    expect(e.mensaje).toBe('El título es obligatorio');
  });

  it('401 en login/registro: usa el mensaje de credenciales incorrectas', () => {
    const e = describirError(error(401, { mensaje: 'Usuario o contraseña incorrectos' }), true);
    expect(e.titulo).toBe('No se pudo iniciar sesión');
    expect(e.mensaje).toBe('Usuario o contraseña incorrectos');
  });

  it('401 en una ruta protegida: indica sesión expirada', () => {
    expect(describirError(error(401)).titulo).toBe('Sesión no válida');
  });

  it('403: sin permisos', () => {
    expect(describirError(error(403)).titulo).toBe('Sin permisos');
  });

  it('409: muestra el mensaje del servidor (p. ej. ISBN duplicado)', () => {
    const e = describirError(error(409, { mensaje: 'Ya existe un libro con el ISBN 978-1' }));
    expect(e.mensaje).toBe('Ya existe un libro con el ISBN 978-1');
  });

  it('un error que no es HTTP devuelve un mensaje genérico', () => {
    expect(describirError(new Error('boom')).titulo).toBe('Error inesperado');
  });
});
