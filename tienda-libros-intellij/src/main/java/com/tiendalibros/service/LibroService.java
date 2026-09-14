package com.tiendalibros.service;

import com.tiendalibros.model.Libro;
import com.tiendalibros.repository.LibroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {
    private final LibroRepository repository;

    public LibroService(LibroRepository repository) {
        this.repository = repository;
    }

    public Libro crear(Libro libro) {
        return repository.guardar(libro);
    }

    public List<Libro> listar() {
        return repository.listar();
    }

    public Libro obtenerPorId(Long id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));
    }

    public Libro actualizar(Long id, Libro libro) {
        return repository.actualizar(id, libro)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));
    }

    public void eliminar(Long id) {
        boolean eliminado = repository.eliminar(id);
        if (!eliminado) {
            throw new RuntimeException("Libro no encontrado");
        }
    }
}
