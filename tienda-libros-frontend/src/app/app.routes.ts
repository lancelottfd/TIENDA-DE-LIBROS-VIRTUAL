import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Register } from './components/register/register';
import { LibrosLista } from './components/libros-lista/libros-lista';
import { LibroForm } from './components/libro-form/libro-form';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'libros', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'libros', component: LibrosLista, canActivate: [authGuard] },
  { path: 'libros/nuevo', component: LibroForm, canActivate: [authGuard] },
  { path: 'libros/editar/:id', component: LibroForm, canActivate: [authGuard] },
  { path: '**', redirectTo: 'libros' },
];
