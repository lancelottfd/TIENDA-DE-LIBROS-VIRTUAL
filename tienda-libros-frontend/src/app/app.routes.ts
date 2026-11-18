import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Register } from './components/register/register';
import { LibrosLista } from './components/libros-lista/libros-lista';
import { LibroForm } from './components/libro-form/libro-form';

export const routes: Routes = [
  { path: '', redirectTo: 'libros', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'libros', component: LibrosLista },
  { path: 'libros/nuevo', component: LibroForm },
  { path: 'libros/editar/:id', component: LibroForm },
  { path: '**', redirectTo: 'libros' },
];
