
package com.tiendalibros.service;

import com.tiendalibros.model.Libro;
import com.tiendalibros.repository.LibroRepository;
import org.springframework.stereotype.Service;
import com.tiendalibros.model.MovimientoInventario;
import com.tiendalibros.repository.MovimientoInventarioRepository;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class LibroService {

    private final LibroRepository repository;
    private final MovimientoInventarioRepository movimientoRepository;

    public LibroService(
            LibroRepository repository,
            MovimientoInventarioRepository movimientoRepository) {

        this.repository = repository;
        this.movimientoRepository = movimientoRepository;
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


    @Transactional
    public MovimientoInventario registrarSalida(
            Long libroId, Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero");
        }

        Libro libro = obtenerPorId(libroId);

        if (libro.getStock() == null ||
                libro.getStock() < cantidad) {

            throw new IllegalArgumentException(
                    "Stock insuficiente");
        }

        libro.setStock(libro.getStock() - cantidad);

        repository.save(libro);

        MovimientoInventario movimiento =
                new MovimientoInventario(
                        libro, cantidad, "SALIDA");

        return movimientoRepository.save(movimiento);
    }
}
