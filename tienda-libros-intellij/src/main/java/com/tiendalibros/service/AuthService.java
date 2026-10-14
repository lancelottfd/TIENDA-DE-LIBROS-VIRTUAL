package com.tiendalibros.service;

import com.tiendalibros.dto.AuthResponse;
import com.tiendalibros.dto.LoginRequest;
import com.tiendalibros.dto.RegisterRequest;
import com.tiendalibros.model.Rol;
import com.tiendalibros.model.Usuario;
import com.tiendalibros.repository.UsuarioRepository;
import com.tiendalibros.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        // Si las credenciales son incorrectas lanza BadCredentialsException (-> 401)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        String token = jwtService.generarToken(
                usuario.getUsername(), usuario.getRol().name());

        return AuthResponse.bearer(token, usuario.getUsername(), usuario.getRol().name());
    }

    public AuthResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        // El registro público SIEMPRE crea un CLIENTE
        Usuario nuevo = new Usuario(
                request.username(),
                passwordEncoder.encode(request.password()),
                Rol.CLIENTE);

        usuarioRepository.save(nuevo);

        String token = jwtService.generarToken(nuevo.getUsername(), nuevo.getRol().name());
        return AuthResponse.bearer(token, nuevo.getUsername(), nuevo.getRol().name());
    }
}
