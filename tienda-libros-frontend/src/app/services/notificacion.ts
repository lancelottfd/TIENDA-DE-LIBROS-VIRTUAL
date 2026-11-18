import { Injectable, signal } from '@angular/core';

export type TipoAviso = 'exito' | 'error' | 'info';

export interface Aviso {
  id: number;
  tipo: TipoAviso;
  titulo: string;
  mensaje?: string;
}

/** Avisos tipo "toast" que se muestran en la esquina de la pantalla (ver componente Toast). */
@Injectable({ providedIn: 'root' })
export class Notificacion {
  private siguienteId = 1;
  private readonly _avisos = signal<Aviso[]>([]);

  readonly avisos = this._avisos.asReadonly();

  exito(titulo: string, mensaje?: string) {
    this.mostrar('exito', titulo, mensaje);
  }

  error(titulo: string, mensaje?: string) {
    this.mostrar('error', titulo, mensaje);
  }

  info(titulo: string, mensaje?: string) {
    this.mostrar('info', titulo, mensaje);
  }

  cerrar(id: number) {
    this._avisos.update((lista) => lista.filter((a) => a.id !== id));
  }

  private mostrar(tipo: TipoAviso, titulo: string, mensaje?: string, duracionMs = 5000) {
    const id = this.siguienteId++;
    this._avisos.update((lista) => [...lista, { id, tipo, titulo, mensaje }]);
    setTimeout(() => this.cerrar(id), duracionMs);
  }
}
