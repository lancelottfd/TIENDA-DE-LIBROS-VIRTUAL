import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Auth } from '../../services/auth';
import { Notificacion } from '../../services/notificacion';
import { describirError } from '../../utils/http-error';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private fb = inject(FormBuilder);
  private auth = inject(Auth);
  private router = inject(Router);
  private notificacion = inject(Notificacion);

  enviando = signal(false);
  errorServidor = signal<string | null>(null);
  registerForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.errorServidor.set(null);

    this.auth.register(this.registerForm.getRawValue()).subscribe({
      next: () => {
        this.notificacion.exito('Cuenta creada', 'Ya iniciaste sesión como CLIENTE.');
        this.router.navigate(['/libros']);
      },
      error: (err) => {
        this.enviando.set(false);
        this.errorServidor.set(describirError(err, true).mensaje);
      },
    });
  }
}
