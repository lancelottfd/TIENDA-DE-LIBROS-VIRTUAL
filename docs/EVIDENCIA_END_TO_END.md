# Evidencia end-to-end: Angular → Spring Boot → MySQL

Guion para demostrar (con capturas o video) que el flujo completo funciona:
el usuario inicia sesión en Angular, el **JWT** viaja en cada solicitud protegida, el backend lo valida
y los datos quedan guardados en MySQL.

```
Navegador (Angular :4200)
   │  1. POST /api/auth/login  {username, password}
   │  2. ◄── {token, rol}      (Angular guarda el JWT en localStorage)
   │  3. GET/POST/PUT/DELETE /api/libros   + Authorization: Bearer <token>   ← interceptor
   ▼
Spring Boot (:8080)  →  CORS → JwtAuthenticationFilter → autorización por rol → Controller → Service
   ▼
MySQL (tienda_libros): usuarios, libros, movimientos_inventario
```

## 1. Cómo levantar todo

| Paso | Comando / acción |
|------|------------------|
| MySQL | Servicio encendido y base `tienda_libros` creada (`CREATE DATABASE tienda_libros;`) |
| Backend | IntelliJ: ejecutar `TiendaLibrosApplication` (o `cd tienda-libros-intellij && mvn spring-boot:run`) → http://localhost:8080 |
| Frontend | `cd tienda-libros-frontend && npm install && npm start` → http://localhost:4200 |

Usuarios de prueba (los crea el backend al iniciar): `admin / admin123` (ADMIN) y `cliente / cliente123` (CLIENTE).

Abre **DevTools (F12)** en el navegador: pestaña **Network** (solicitudes) y **Application → Local Storage** (token).

## 2. Escenario de demostración

| # | Qué haces en Angular | Qué debe verse (evidencia) | Verificación en MySQL |
|---|----------------------|----------------------------|-----------------------|
| 1 | Abrir `http://localhost:4200` **sin sesión** | Te redirige a `/login` (guard de rutas). | — |
| 2 | Iniciar sesión con `admin / admin123` | Network: `POST /api/auth/login` → **200** con `token`. Local Storage: clave `tienda-libros-sesion` con el JWT. Barra superior: `admin` + badge `ADMIN`. | Consulta **1**: usuarios con contraseña en hash BCrypt (`$2a$10$…`). |
| 3 | Se carga el catálogo | Network: `GET /api/libros` → **200**; en *Request Headers*: `Authorization: Bearer eyJ…`. | Consulta **2**: mismos libros que la tabla. |
| 4 | **Nuevo libro** (ADMIN) y guardar | `POST /api/libros` → **201** con header Authorization; aviso "Libro creado". | Consulta **2**: aparece el libro nuevo. |
| 5 | **Editar** ese libro (cambiar precio/stock) | `PUT /api/libros/{id}` → **200**. | Consulta **2**: valores actualizados. |
| 6 | Pulsar **Comprar** con cantidad 2 | `POST /api/libros/{id}/salida?cantidad=2` → **201**; el stock baja en la tabla. | Consulta **3**: fila `SALIDA` con cantidad 2; consulta **2**: stock menor. |
| 7 | Cerrar sesión y entrar con `cliente / cliente123` | Badge `CLIENTE`; **no** aparecen "Nuevo libro", "Editar" ni "Eliminar". "Comprar" sí funciona (`201`). | Consulta **3**: nuevo movimiento. |
| 8 | Como CLIENTE, escribir en la URL `/libros/nuevo` | Te devuelve al catálogo con aviso "Acceso restringido" (guard por rol). | — |
| 9 | Como CLIENTE, probar el **backend directo** (consola de DevTools, ver snippet A) | Responde **403**: aunque se salte la interfaz, la API protege la ruta. | Consulta **4**: el conteo de libros no cambió. |
| 10 | Token inválido (snippet B) y recargar la página | `GET /api/libros` → **401**; Angular cierra sesión sola y vuelve a `/login`. | — |
| 11 | Volver como ADMIN y **Eliminar** un libro | `DELETE /api/libros/{id}` → **204**; desaparece de la tabla. | Consulta **2**: ya no existe. |

Las consultas están numeradas en [`consultas-evidencia.sql`](consultas-evidencia.sql).

**Snippet A** (consola de DevTools, logueado como CLIENTE) — intenta crear un libro saltándose la interfaz:

```js
const s = JSON.parse(localStorage.getItem('tienda-libros-sesion'));
fetch('http://localhost:8080/api/libros', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + s.token },
  body: JSON.stringify({ titulo: 'Prueba', autor: 'Autor X', isbn: '123-4567890', precio: 10, stock: 5 }),
}).then(r => console.log('Estado HTTP:', r.status));   // esperado: 403
```

**Snippet B** — corrompe la firma del token para simular un token inválido:

```js
const s = JSON.parse(localStorage.getItem('tienda-libros-sesion'));
s.token = s.token + 'x';
localStorage.setItem('tienda-libros-sesion', JSON.stringify(s));
location.reload();   // esperado: 401 y regreso a /login
```

En la consola del backend (IntelliJ) se ven además las sentencias SQL (`spring.jpa.show-sql=true`),
útiles como evidencia de que cada acción llega hasta la base de datos.

## 3. Capturas sugeridas para el informe

| Archivo sugerido | Qué debe mostrar |
|------------------|------------------|
| `e2e-01-login.png` | Pantalla de login y, en Network, `POST /api/auth/login` 200 |
| `e2e-02-token-localstorage.png` | Local Storage con `tienda-libros-sesion` |
| `e2e-03-header-bearer.png` | Request Headers de `GET /api/libros` con `Authorization: Bearer …` |
| `e2e-04-crear-libro.png` | Formulario/aviso de creación + `POST /api/libros` 201 |
| `e2e-05-mysql-libros.png` | Consulta 2 en MySQL Workbench con el libro creado |
| `e2e-06-compra-movimiento.png` | Botón Comprar + consulta 3 con la fila `SALIDA` |
| `e2e-07-vista-cliente.png` | Catálogo como CLIENTE (sin botones de administración) |
| `e2e-08-403-cliente.png` | Snippet A devolviendo 403 |
| `e2e-09-401-token-invalido.png` | Snippet B: 401 y regreso a login |
| `e2e-10-tests.png` | `mvn test` y `npm test` en verde |

## 4. Pruebas automáticas relacionadas

```bash
# Backend (incluye CorsAngularTest: Angular en :4200 puede llamar a la API con Authorization)
cd tienda-libros-intellij && mvn test

# Frontend (Vitest): sesión/JWT, interceptor, guards y manejo de errores
cd tienda-libros-frontend && npm test
```

## 5. Problemas frecuentes

| Síntoma | Causa probable |
|---------|----------------|
| Aviso "Sin conexión… puerto 8080" | El backend no está levantado o MySQL no responde |
| Error CORS en la consola del navegador | Angular no corre en `localhost:4200` / `127.0.0.1:4200` (ver `SecurityConfig.corsConfigurationSource`) |
| Login responde 401 | Usuario o contraseña incorrectos (`admin / admin123`) |
| `ng` no se reconoce | Falta ejecutar `npm install` dentro de `tienda-libros-frontend` |
| Backend no inicia por MySQL | Revisar usuario/clave en `application.properties` y que exista la BD `tienda_libros` |
