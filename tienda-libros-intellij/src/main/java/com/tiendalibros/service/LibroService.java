
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
        libro.setId(null);
        return repository.save(libro);
    }

    public List<Libro> listar() {
        return repository.findAll();
    }

    public Libro obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Libro no encontrado"));
    }

    public Libro actualizar(Long id, Libro datos) {
        Libro libro = obtenerPorId(id);

        libro.setTitulo(datos.getTitulo());
        libro.setAutor(datos.getAutor());
        libro.setPrecio(datos.getPrecio());
        libro.setStock(datos.getStock());
        libro.setCategoriaId(datos.getCategoriaId());

        return repository.save(libro);
    }

    public void eliminar(Long id) {
        Libro libro = obtenerPorId(id);
        repository.delete(libro);
    }

    public List<Libro> buscarLibrosConStockBajo(Integer cantidad) {
        return repository.buscarLibrosConStockBajo(cantidad);
    }

}
