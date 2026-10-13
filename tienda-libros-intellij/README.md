# tienda-libros

Proyecto Maven con Spring Boot 3.5.5 y Java 17.

## Abrir en IntelliJ IDEA
1. Extrae el ZIP.
2. En IntelliJ IDEA selecciona **Open** y abre la carpeta `tienda-libros`.
3. Espera a que Maven descargue las dependencias.
4. Ejecuta `TiendaLibrosApplication`.

## API
- GET `/api/libros`
- GET `/api/libros/{id}`
- POST `/api/libros`
- PUT `/api/libros/{id}`
- DELETE `/api/libros/{id}`

Los datos se almacenan en memoria mediante `LibroRepository`.
