package com.tiendalibros.repository;

import com.tiendalibros.model.Libro;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class LibroRepository {
    private final List<Libro> libros = new ArrayList<>();
    private final AtomicLong contador = new AtomicLong(0);

    public Libro guardar(Libro libro) {
        libro.setId(contador.incrementAndGet());
        libros.add(libro);
        return libro;
    }

    public List<Libro> listar() {
        return new ArrayList<>(libros);
    }

    public Optional<Libro> buscarPorId(Long id) {
        return libros.stream()
                .filter(libro -> libro.getId().equals(id))
                .findFirst();
    }

    public Optional<Libro> actualizar(Long id, Libro datos) {
        Optional<Libro> resultado = buscarPorId(id);

        if (resultado.isPresent()) {
            Libro libro = resultado.get();
            libro.setTitulo(datos.getTitulo());
            libro.setAutor(datos.getAutor());
            libro.setPrecio(datos.getPrecio());
            libro.setStock(datos.getStock());
            libro.setCategoriaId(datos.getCategoriaId());
        }

        return resultado;
    }

    public boolean eliminar(Long id) {
        return libros.removeIf(libro -> libro.getId().equals(id));
    }
}
