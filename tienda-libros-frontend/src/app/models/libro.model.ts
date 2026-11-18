/** Libro tal como lo expone la API (/api/libros). */
export interface Libro {
  id?: number;
  titulo: string;
  autor: string;
  isbn?: string | null;
  precio: number;
  stock: number;
  categoriaId?: number | null;
}

/** Respuesta de POST /api/libros/{id}/salida */
export interface MovimientoInventario {
  id: number;
  libro: Libro;
  cantidad: number;
  tipo: string; // "SALIDA"
  fecha: string;
}
