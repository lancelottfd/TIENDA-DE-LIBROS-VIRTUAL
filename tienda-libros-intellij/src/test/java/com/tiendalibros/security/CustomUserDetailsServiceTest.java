package com.tiendalibros.security;

import com.tiendalibros.model.Rol;
import com.tiendalibros.model.Usuario;
import com.tiendalibros.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final CustomUserDetailsService service =
            new CustomUserDetailsService(repository);

    @Test
    void debeCargarUsuarioConAutoridadSegunSuRol() {
        when(repository.findByUsername("admin"))
                .thenReturn(Optional.of(new Usuario("admin", "hash", Rol.ADMIN)));

        UserDetails detalles = service.loadUserByUsername("admin");

        assertEquals("admin", detalles.getUsername());
        assertEquals("hash", detalles.getPassword());
        assertTrue(detalles.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void debeLanzarExcepcionSiElUsuarioNoExiste() {
        when(repository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("fantasma"));
    }
}
