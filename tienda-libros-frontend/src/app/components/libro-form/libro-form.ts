import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Libro } from '../../models/libro.model';
import { LibroService } from '../../services/libro';
import { Notificacion } from '../../services/notificacion';

@Component({
  selector: 'app-libro-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './libro-form.html',
  styleUrl: './libro-form.css',
})
export class LibroForm implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private libroService = inject(LibroService);
  private notificacion = inject(Notificacion);

  libroId = signal<number | null>(null);
  cargando = signal(false);
  guardando = signal(false);

  /** Se conserva al editar para no perder la categoría del libro en el PUT. */
  private categoriaId: number | null = null;

  libroForm = this.fb.group({
    titulo: ['', [Validators.required, Validators.minLength(2)]],
    autor: ['', [Validators.required, Validators.minLength(3)]],
    isbn: ['', [Validators.required, Validators.pattern(/^[0-9-]{10,17}$/)]],
    precio: [null as number | null, [Validators.required, Validators.min(0.1)]],
    stock: [
      null as number | null,
      [Validators.required, Validators.min(0), Validators.pattern(/^[0-9]+$/)],
    ],
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;

    this.libroId.set(Number(id));
    this.cargando.set(true);

    // GET /api/libros/{id}  (si no existe -> 404, el interceptor avisa y volvemos a la lista)
    this.libroService.obtener(Number(id)).subscribe({
      next: (libro) => {
        this.categoriaId = libro.categoriaId ?? null;
        this.libroForm.patchValue({
          titulo: libro.titulo,
          autor: libro.autor,
          isbn: libro.isbn ?? '',
          precio: libro.precio,
          stock: libro.stock,
        });
        this.cargando.set(false);
      },
      error: () => this.router.navigate(['/libros']),
    });
  }

  onSubmit() {
    if (this.libroForm.invalid) {
      this.libroForm.markAllAsTouched();
      return;
    }

    const v = this.libroForm.getRawValue();
    const libro: Libro = {
      titulo: v.titulo!.trim(),
      autor: v.autor!.trim(),
      isbn: v.isbn!.trim(),
      precio: Number(v.precio),
      stock: Number(v.stock),
      categoriaId: this.categoriaId,
    };

    this.guardando.set(true);

    const id = this.libroId();
    // CREATE: POST /api/libros   |   UPDATE: PUT /api/libros/{id}
    const peticion = id ? this.libroService.actualizar(id, libro) : this.libroService.crear(libro);

    peticion.subscribe({
      next: (guardado) => {
        this.notificacion.exito(
          id ? 'Libro actualizado' : 'Libro creado',
          `"${guardado.titulo}" se guardó correctamente.`,
        );
        this.router.navigate(['/libros']);
      },
      // Errores 400/403/409... ya fueron mostrados por el interceptor; el formulario queda para corregir.
      error: () => this.guardando.set(false),
    });
  }
}
