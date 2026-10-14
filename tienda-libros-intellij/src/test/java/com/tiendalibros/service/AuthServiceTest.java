package com.tiendalibros.service;

import com.tiendalibros.dto.AuthResponse;
import com.tiendalibros.dto.LoginRequest;
import com.tiendalibros.dto.RegisterRequest;
import com.tiendalibros.model.Rol;
import com.tiendalibros.model.Usuario;
import com.tiendalibros.repository.UsuarioRepository;
import com.tiendalibros.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService service;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        usuarioRepository = mock(UsuarioRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService(
                "clave-secreta-de-pruebas-con-mas-de-32-caracteres-123", 60_000);

        service = new AuthService(
                authenticationManager, usuarioRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginCorrectoDebeDevolverTokenConElRolDelUsuario() {
        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.of(new Usuario("admin", "hash", Rol.ADMIN)));

        AuthResponse respuesta = service.login(new LoginRequest("admin", "admin123"));

        assertEquals("Bearer", respuesta.tipo());
        assertEquals("admin", respuesta.username());
        assertEquals("ADMIN", respuesta.rol());
        assertTrue(jwtService.esTokenValido(respuesta.token()));
        assertEquals("admin", jwtService.extraerUsername(respuesta.token()));
        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void loginConCredencialesIncorrectasDebePropagarBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("mal"));

        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("admin", "incorrecta")));

        verify(usuarioRepository, never()).findByUsername(any());
    }

    @Test
    void registroDebeCrearClienteConPasswordEncriptada() {
        when(usuarioRepository.existsByUsername("maria")).thenReturn(false);

        AuthResponse respuesta = service.registrar(
                new RegisterRequest("maria", "secreto123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertEquals("maria", guardado.getUsername());
        assertEquals(Rol.CLIENTE, guardado.getRol());
        assertNotEquals("secreto123", guardado.getPassword());
        assertTrue(passwordEncoder.matches("secreto123", guardado.getPassword()));
        assertEquals("CLIENTE", respuesta.rol());
        assertTrue(jwtService.esTokenValido(respuesta.token()));
    }

    @Test
    void registroConUsuarioExistenteDebeFallar() {
        when(usuarioRepository.existsByUsername("admin")).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrar(new RegisterRequest("admin", "secreto123")));

        assertEquals("El usuario ya existe", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }
}
