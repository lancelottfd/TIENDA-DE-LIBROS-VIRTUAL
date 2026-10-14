# Gestión de Inventario

API REST con Spring Boot 3.5.5, Java 17, MySQL, Spring Security y JWT.

## Abrir en IntelliJ IDEA
1. Extrae el ZIP y abre la carpeta `tienda-libros-intellij` con **Open**.
2. Espera a que Maven descargue las dependencias.
3. Crea las bases de datos en MySQL: `tienda_libros` y `tienda_libros_test`.
4. Ejecuta `TiendaLibrosApplication`.

## Seguridad

Autenticación **JWT** (stateless) con **dos roles**:

| Rol       | Permisos                                                                 |
|-----------|--------------------------------------------------------------------------|
| `ADMIN`   | Todo: crear, modificar y eliminar libros, stock bajo, salidas.           |
| `CLIENTE` | Consultar libros (`GET`) y registrar salidas/compras (`POST .../salida`). |

Usuarios de prueba (se crean solos al iniciar, con contraseña BCrypt):

| Usuario   | Contraseña   | Rol       |
|-----------|--------------|-----------|
| `admin`   | `admin123`   | `ADMIN`   |
| `cliente` | `cliente123` | `CLIENTE` |

### Endpoints

| Método | Ruta                        | Acceso            |
|--------|-----------------------------|-------------------|
| POST   | `/api/auth/login`           | Público           |
| POST   | `/api/auth/register`        | Público (crea CLIENTE) |
| GET    | `/api/libros`, `/api/libros/{id}` | ADMIN, CLIENTE |
| POST   | `/api/libros/{id}/salida?cantidad=n` | ADMIN, CLIENTE |
| GET    | `/api/libros/stock-bajo?cantidad=n`  | ADMIN |
| POST   | `/api/libros`               | ADMIN             |
| PUT    | `/api/libros/{id}`          | ADMIN             |
| DELETE | `/api/libros/{id}`          | ADMIN             |

Respuestas: `401` sin token / token inválido o expirado, `403` con token válido pero sin el rol necesario.

### Uso
1. `POST /api/auth/login` con `{"username":"admin","password":"admin123"}` → devuelve `token`.
2. Enviar el header `Authorization: Bearer <token>` en las demás peticiones.

### Configuración (`application.properties`)
- `jwt.secret`: clave de firma (mínimo 32 caracteres). Se puede sobreescribir con la variable de entorno `JWT_SECRET`.
- `jwt.expiration-ms`: duración del token (por defecto 1 hora).

## Pruebas
```
mvn test
```
- `JwtServiceTest`, `CustomUserDetailsServiceTest`, `AuthServiceTest`: unitarias.
- `SeguridadRutasTest`: 401/403/200 por rol sobre los controladores (sin BD).
- `FlujoAutenticacionIntegrationTest`, `LibroRollbackIntegrationTest`: requieren MySQL con la BD `tienda_libros_test`.
- Las pruebas previas (`LibroServiceTest`, `LibroControllerTest`, `LibroTransaccionTest`) se mantienen sin cambios.

## Postman
Importar `Tienda_Libros_Postman_Collection.json` y ejecutarla con el **Collection Runner** (en orden).
Los logins de la carpeta 1 guardan `tokenAdmin` y `tokenCliente` automáticamente.
