package com.utec.sienep.service;

import com.utec.sienep.entity.Rol;
import com.utec.sienep.entity.Usuario;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.RolRepository;
import com.utec.sienep.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.HashSet;
import java.util.Set;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminServiceTest {

    @Mock private RolRepository rolRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private AdminService adminService;

    private Rol rolAdmin;
    private Rol rolDocente;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Configurar SecurityContext con usuario autenticado
        var auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Datos de prueba
        rolAdmin = new Rol();
        rolAdmin.setId(1L);
        rolAdmin.setNombre("ROLE_ADMIN");
        rolAdmin.setDescripcion("Administrador del sistema");

        rolDocente = new Rol();
        rolDocente.setId(2L);
        rolDocente.setNombre("ROLE_DOCENTE");
        rolDocente.setDescripcion("Docente");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("juanmoreira");
        usuario.setNombre("Juan");
        usuario.setApellido("Moreira");
        usuario.setActivo(true);
        // Usamos ArrayList para que sea mutable (add/remove funcionan)
        usuario.setRoles(new HashSet<>(Set.of(rolAdmin)));

        // AuditoriaService no hace nada en tests
        doNothing().when(auditoriaService).registrarExitoso(anyString(), anyString(), anyString(), any(), anyString());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // CrearRol

    @Test
    void crear_rol_nuevo_retorna_rol_guardado() {
        when(rolRepository.findByNombre("ROLE_SUPERVISOR")).thenReturn(Optional.empty());
        Rol rolNuevo = new Rol();
        rolNuevo.setId(10L);
        rolNuevo.setNombre("ROLE_SUPERVISOR");
        rolNuevo.setDescripcion("Supervisor");
        when(rolRepository.save(any(Rol.class))).thenReturn(rolNuevo);

        Rol resultado = adminService.crearRol("SUPERVISOR", "Supervisor");

        assertEquals("ROLE_SUPERVISOR", resultado.getNombre());
        verify(rolRepository).save(any(Rol.class));
    }

    @Test
    void crear_rol_agrega_prefijo_role_si_no_lo_tiene() {
        when(rolRepository.findByNombre("ROLE_AUDITOR")).thenReturn(Optional.empty());
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));

        // Pasamos sin prefijo
        Rol resultado = adminService.crearRol("auditor", "Auditor");

        assertEquals("ROLE_AUDITOR", resultado.getNombre());
    }

    @Test
    void crear_rol_duplicado_lanza_excepcion() {
        when(rolRepository.findByNombre("ROLE_ADMIN")).thenReturn(Optional.of(rolAdmin));

        assertThrows(ReglaNegocioException.class, () -> adminService.crearRol("ADMIN", "Administrador"));
        verify(rolRepository, never()).save(any());
    }

    // AsignarRol

    @Test
    void asignar_rol_nuevo_al_usuario_exitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolDocente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // usuario actualmente solo tiene ROLE_ADMIN, asignamos ROLE_DOCENTE
        assertDoesNotThrow(() -> adminService.asignarRol(1L, 2L));
        assertTrue(usuario.getRoles().contains(rolDocente));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void asignar_rol_ya_existente_lanza_excepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin));

        // usuario ya tiene ROLE_ADMIN
        assertThrows(ReglaNegocioException.class, () -> adminService.asignarRol(1L, 1L));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void asignar_rol_usuario_inexistente_lanza_excepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> adminService.asignarRol(99L, 1L));
    }

    // QuitarRol

    @Test
    void quitar_rol_cuando_tiene_mas_de_uno_exitoso() {
        // Agregar un segundo rol para que pueda quitarse uno
        usuario.getRoles().add(rolDocente);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolDocente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        assertDoesNotThrow(() -> adminService.quitarRol(1L, 2L));
        assertFalse(usuario.getRoles().contains(rolDocente));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void quitar_unico_rol_lanza_excepcion() {
        // usuario solo tiene ROLE_ADMIN
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolAdmin));

        assertThrows(ReglaNegocioException.class, () -> adminService.quitarRol(1L, 1L));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void quitar_rol_no_asignado_lanza_excepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolDocente));

        // usuario no tiene ROLE_DOCENTE
        assertThrows(ReglaNegocioException.class, () -> adminService.quitarRol(1L, 2L));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void quitar_rol_usuario_inexistente_lanza_excepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> adminService.quitarRol(99L, 1L));
    }
}
