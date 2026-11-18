import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../config';
import { Libro, MovimientoInventario } from '../models/libro.model';

/** Cliente de la API REST /api/libros (Spring Boot). */
@Injectable({ providedIn: 'root' })
export class LibroService {
  private http = inject(HttpClient);
  private url = `${API_URL}/libros`;

  listar() {
    return this.http.get<Libro[]>(this.url);
  }

  obtener(id: number) {
    return this.http.get<Libro>(`${this.url}/${id}`);
  }

  crear(libro: Libro) {
    return this.http.post<Libro>(this.url, libro);
  }

  actualizar(id: number, libro: Libro) {
    return this.http.put<Libro>(`${this.url}/${id}`, libro);
  }

  eliminar(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  registrarSalida(id: number, cantidad: number) {
    const params = new HttpParams().set('cantidad', cantidad);
    return this.http.post<MovimientoInventario>(`${this.url}/${id}/salida`, null, { params });
  }
}
