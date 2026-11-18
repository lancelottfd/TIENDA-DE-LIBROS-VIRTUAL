# Tienda de Libros – Frontend (Angular)

Cliente web de la API REST de Spring Boot (`tienda-libros-intellij`).

## Autenticación con JWT

- **Login / registro:** `/login` y `/register` llaman a `POST /api/auth/login` y `/api/auth/register`.
- **Sesión:** `services/auth.ts` guarda el JWT (y rol/usuario) en un signal y en `localStorage` (`tienda-libros-sesion`); si el token ya expiró no se restaura.
- **Envío del JWT:** `interceptors/auth-interceptor.ts` agrega `Authorization: Bearer <token>` a toda solicitud a `http://localhost:8080/api` (excepto `/api/auth/*`). Ante un `401` cierra la sesión y vuelve al login.
- **Rutas protegidas:** `guards/auth-guard.ts` (`authGuard`: requiere sesión; `adminGuard`: solo rol ADMIN para crear/editar libros).
- **Interfaz por rol:** el CLIENTE solo ve el catálogo y "Comprar"; el ADMIN ve además crear, editar y eliminar.

Usuarios de prueba: `admin / admin123` (ADMIN) y `cliente / cliente123` (CLIENTE).

## Cómo ejecutarlo

1. Levantar MySQL y el backend (puerto 8080).
2. En esta carpeta: `npm install` y luego `npm start` → http://localhost:4200
3. Pruebas: `npm test`

La guía de demostración completa (Angular → backend → base de datos) está en
[`../docs/EVIDENCIA_END_TO_END.md`](../docs/EVIDENCIA_END_TO_END.md).

---

# TiendaLibrosFrontend

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.2.13.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
