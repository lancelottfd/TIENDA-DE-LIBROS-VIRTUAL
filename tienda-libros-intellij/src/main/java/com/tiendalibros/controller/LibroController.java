package com.tiendalibros.controller;

import com.tiendalibros.model.Libro;
import com.tiendalibros.service.LibroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {
    private final LibroService service;

    public LibroController(LibroService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Libro> crear(@RequestBody Libro libro) {
        Libro nuevoLibro = service.crear(libro);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoLibro);
    }

    @GetMapping
    public ResponseEntity<List<Libro>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Libro> actualizar(
            @PathVariable Long id,
            @RequestBody Libro libro) {
        return ResponseEntity.ok(service.actualizar(id, libro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
