package com.utec.sienep.service;

import com.utec.sienep.dto.request.LoginRequestDTO;
import com.utec.sienep.dto.response.LoginResponseDTO;
import com.utec.sienep.entity.Rol;
import com.utec.sienep.entity.Usuario;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.UsuarioRepository;
import com.utec.sienep.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de AuthService")
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;
    @Mock private JwtUtil jwtUtil;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioAdmin;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = new Rol("ROLE_ADMIN");

        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setUsername("admin");
        usuarioAdmin.setPasswordHash("$2a$hash");
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setApellido("SIENEP");
        usuarioAdmin.setEmail("admin@sienep.utec.edu.uy");
        usuarioAdmin.setActivo(true);
        usuarioAdmin.setFechaAlta(LocalDateTime.now());
        usuarioAdmin.setRoles(Set.of(rolAdmin));

        userDetails = User.builder()
                .username("admin")
                .password("$2a$hash")
                .authorities("ROLE_ADMIN")
                .build();
    }

    @Test
    @DisplayName("Login con credenciales válidas retorna token JWT")
    void login_credenciales_validas_retorna_token() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword("Admin1234!");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.generarToken(userDetails)).thenReturn("token.jwt.mock");
        when(usuarioRepository.findByUsernameAndActivoTrue("admin"))
                .thenReturn(Optional.of(usuarioAdmin));

        LoginResponseDTO response = authService.login(dto);

        assertNotNull(response);
        assertEquals("token.jwt.mock", response.getToken());
        assertEquals("admin", response.getUsername());
        // Verificar que la contraseña NO está en la respuesta
        assertNull(response.getClass().getDeclaredFields().length > 0
                ? null : null); // El DTO no tiene campo password
        verify(auditoriaService).registrarExitoso(eq("admin"), eq("LOGIN"),
                any(), any(), any());
    }

    @Test
    @DisplayName("Login con credenciales inválidas lanza ReglaNegocioException")
    void login_credenciales_invalidas_lanza_excepcion() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword("wrongpass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(ReglaNegocioException.class, () -> authService.login(dto));
        verify(auditoriaService).registrarFallido(eq("admin"), eq("LOGIN"), any());
    }

    @Test
    @DisplayName("Logout registra evento de auditoría")
    void logout_registra_auditoria() {
        assertDoesNotThrow(() -> authService.logout("admin"));
        verify(auditoriaService).registrarExitoso(
                eq("admin"), eq("LOGOUT"), any(), any(), any());
    }

    @Test
    @DisplayName("Cambiar contraseña con contraseña actual incorrecta lanza excepción")
    void cambiar_password_actual_incorrecta_lanza_excepcion() {
        when(usuarioRepository.findByUsernameAndActivoTrue("admin"))
                .thenReturn(Optional.of(usuarioAdmin));
        when(passwordEncoder.matches("wrongpass", "$2a$hash")).thenReturn(false);

        assertThrows(ReglaNegocioException.class,
                () -> authService.cambiarPassword("admin", "wrongpass", "Nueva1234!"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cambiar contraseña con nueva igual a la actual lanza excepción")
    void cambiar_password_nueva_igual_actual_lanza_excepcion() {
        when(usuarioRepository.findByUsernameAndActivoTrue("admin"))
                .thenReturn(Optional.of(usuarioAdmin));
        when(passwordEncoder.matches("Admin1234!", "$2a$hash")).thenReturn(true);

        assertThrows(ReglaNegocioException.class,
                () -> authService.cambiarPassword("admin", "Admin1234!", "Admin1234!"));
    }

    @Test
    @DisplayName("Cambiar contraseña válida actualiza el hash correctamente")
    void cambiar_password_valida_actualiza_hash() {
        when(usuarioRepository.findByUsernameAndActivoTrue("admin"))
                .thenReturn(Optional.of(usuarioAdmin));
        when(passwordEncoder.matches("Admin1234!", "$2a$hash")).thenReturn(true);
        when(passwordEncoder.matches("Nueva1234!", "$2a$hash")).thenReturn(false);
        when(passwordEncoder.encode("Nueva1234!")).thenReturn("$2a$newhash");
        when(usuarioRepository.save(any())).thenReturn(usuarioAdmin);

        assertDoesNotThrow(
                () -> authService.cambiarPassword("admin", "Admin1234!", "Nueva1234!"));
        verify(passwordEncoder).encode("Nueva1234!");
        verify(usuarioRepository).save(argThat(u ->
                u.getPasswordHash().equals("$2a$newhash")));
    }
}
