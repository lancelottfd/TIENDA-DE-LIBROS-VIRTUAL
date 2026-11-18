package com.tiendalibros.security;

import com.tiendalibros.model.Rol;
import com.tiendalibros.model.Usuario;
import com.tiendalibros.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea los usuarios de prueba (uno por rol) si todavía no existen.
 * Las contraseñas se guardan con BCrypt.
 *
 *   admin   / admin123    -> ROLE ADMIN
 *   cliente / cliente123  -> ROLE CLIENTE
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin", "admin123", Rol.ADMIN);
        crearSiNoExiste("cliente", "cliente123", Rol.CLIENTE);
    }

    private void crearSiNoExiste(String username, String passwordPlano, Rol rol) {
        if (!usuarioRepository.existsByUsername(username)) {
            usuarioRepository.save(
                    new Usuario(username, passwordEncoder.encode(passwordPlano), rol));
        }
    }
}
