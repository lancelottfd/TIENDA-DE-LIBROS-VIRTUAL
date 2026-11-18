import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Libro } from '../../models/libro.model';
import { Auth } from '../../services/auth';
import { LibroService } from '../../services/libro';
import { Notificacion } from '../../services/notificacion';

@Component({
  selector: 'app-libros-lista',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './libros-lista.html',
  styleUrl: './libros-lista.css',
})
export class LibrosLista implements OnInit {
  private libroService = inject(LibroService);
  private notificacion = inject(Notificacion);
  auth = inject(Auth);

  libros = signal<Libro[]>([]);
  cargando = signal(true);
  errorCarga = signal(false);
  procesandoId = signal<number | null>(null);
  busqueda = signal('');
  cantidades = signal<Record<number, number>>({});

  librosFiltrados = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    if (!q) return this.libros();
    return this.libros().filter((l) =>
      [l.titulo, l.autor, l.isbn ?? ''].some((v) => v.toLowerCase().includes(q)),
    );
  });

  ngOnInit() {
    this.cargar();
  }

  /** READ: GET /api/libros */
  cargar() {
    this.cargando.set(true);
    this.errorCarga.set(false);
    this.libroService.listar().subscribe({
      next: (libros) => {
        this.libros.set(libros);
        this.cargando.set(false);
      },
      error: () => {
        // El interceptor ya mostró el aviso con el error HTTP.
        this.cargando.set(false);
        this.errorCarga.set(true);
      },
    });
  }

  cantidadDe(libro: Libro): number {
    return this.cantidades()[libro.id!] ?? 1;
  }

  cambiarCantidad(libro: Libro, valor: string) {
    this.cantidades.update((c) => ({ ...c, [libro.id!]: Number(valor) }));
  }

  registrarSalida(libro: Libro) {
    const cantidad = this.cantidadDe(libro);
    this.procesandoId.set(libro.id!);
    this.libroService.registrarSalida(libro.id!, cantidad).subscribe({
      next: () => {
        this.procesandoId.set(null);
        this.notificacion.exito('Salida registrada', `Se descontaron ${cantidad} unidad(es) de "${libro.titulo}".`);
        this.cargar();
      },
      error: (err) => {
        this.procesandoId.set(null);
        if (err.status === 404) this.cargar();
      },
    });
  }

  eliminar(libro: Libro) {
    if (!confirm(`¿Eliminar "${libro.titulo}"? Esta acción no se puede deshacer.`)) return;

    this.procesandoId.set(libro.id!);
    this.libroService.eliminar(libro.id!).subscribe({
      next: () => {
        this.procesandoId.set(null);
        this.libros.update((ls) => ls.filter((l) => l.id !== libro.id));
        this.notificacion.exito('Libro eliminado', `"${libro.titulo}" fue eliminado.`);
      },
      error: (err) => {
        this.procesandoId.set(null);

        if (err.status === 404) this.cargar();
      },
    });
  }
}
