import { Component, inject } from '@angular/core';
import { Notificacion } from '../../services/notificacion';

@Component({
  selector: 'app-toast',
  template: `
    <div class="toast-contenedor" aria-live="polite">
      @for (aviso of notificacion.avisos(); track aviso.id) {
        <div [class]="'toast toast-' + aviso.tipo" role="alert">
          <div class="toast-texto">
            <strong>{{ aviso.titulo }}</strong>
            @if (aviso.mensaje) {
              <span>{{ aviso.mensaje }}</span>
            }
          </div>
          <button type="button" class="toast-cerrar" (click)="notificacion.cerrar(aviso.id)" aria-label="Cerrar">×</button>
        </div>
      }
    </div>
  `,
})
export class Toast {
  notificacion = inject(Notificacion);
}
