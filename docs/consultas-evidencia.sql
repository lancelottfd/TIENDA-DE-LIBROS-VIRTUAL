-- ============================================================
-- Consultas para la evidencia end-to-end (ejecutar en MySQL Workbench)
-- Base de datos de la aplicación: tienda_libros
-- ============================================================
USE tienda_libros;

-- 1) Usuarios y roles. La contraseña debe verse como hash BCrypt ($2a$10$...), nunca en texto plano.
SELECT id, username, rol, LEFT(password, 20) AS password_hash
FROM usuarios;

-- 2) Catálogo actual (ver el libro creado/editado desde Angular)
SELECT id, titulo, autor, isbn, precio, stock
FROM libros
ORDER BY id DESC;

-- 3) Movimientos de inventario (se crean al pulsar "Comprar" en Angular)
SELECT m.id, l.titulo, m.cantidad, m.tipo, m.fecha
FROM movimientos_inventario m
JOIN libros l ON l.id = m.libro_id
ORDER BY m.id DESC;

-- 4) Resumen rápido para comparar antes/después de cada acción
SELECT
  (SELECT COUNT(*) FROM usuarios)               AS usuarios,
  (SELECT COUNT(*) FROM libros)                 AS libros,
  (SELECT COUNT(*) FROM movimientos_inventario) AS movimientos;
