import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

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

  libroId: string | null = null;

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
    this.libroId = this.route.snapshot.paramMap.get('id');
  }

  onSubmit() {
    if (this.libroForm.invalid) {
      this.libroForm.markAllAsTouched();
      return;
    }
    console.log('Datos válidos del Libro:', this.libroForm.value);
  }
}
