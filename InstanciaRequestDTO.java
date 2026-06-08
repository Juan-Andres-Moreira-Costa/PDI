package com.utec.sienep.service;

import com.utec.sienep.dto.request.IncidenciaRequestDTO;
import com.utec.sienep.dto.response.IncidenciaResponseDTO;
import com.utec.sienep.entity.*;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de IncidenciaService")
class IncidenciaServiceTest {

    @Mock private IncidenciaRepository incidenciaRepository;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private InstanciaRepository instanciaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private IncidenciaService incidenciaService;

    private Estudiante estudianteActivo;
    private Incidencia incidenciaExistente;

    @BeforeEach
    void setUp() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        SecurityContext secCtx = mock(SecurityContext.class);
        when(secCtx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(secCtx);

        estudianteActivo = new Estudiante();
        estudianteActivo.setId(1L);
        estudianteActivo.setCedula("12345670");
        estudianteActivo.setNombre("Juan");
        estudianteActivo.setApellido("Pérez");
        estudianteActivo.setEmail("juan@utec.edu.uy");
        estudianteActivo.setFechaNacimiento(LocalDate.now().minusYears(20));
        estudianteActivo.setActivo(true);
        estudianteActivo.setFechaAlta(LocalDateTime.now());

        incidenciaExistente = new Incidencia();
        incidenciaExistente.setId(1L);
        incidenciaExistente.setEstudiante(estudianteActivo);
        incidenciaExistente.setTitulo("Falta injustificada reiterada");
        incidenciaExistente.setDescripcion("El estudiante acumula 3 faltas consecutivas.");
        incidenciaExistente.setSeveridad("ALTA");
        incidenciaExistente.setEstado("ABIERTA");
        incidenciaExistente.setFechaIncidencia(LocalDateTime.now());
        incidenciaExistente.setActivo(true);
        incidenciaExistente.setFechaAlta(LocalDateTime.now());
    }

    // ─── Registro ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Registrar incidencia válida retorna DTO correctamente")
    void registrar_incidencia_valida_retorna_dto() {
        IncidenciaRequestDTO dto = buildDTO("ALTA");

        when(estudianteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(estudianteActivo));
        when(usuarioRepository.findByUsernameAndActivoTrue("admin"))
                .thenReturn(Optional.empty());
        when(incidenciaRepository.save(any(Incidencia.class)))
                .thenReturn(incidenciaExistente);

        IncidenciaResponseDTO resultado = incidenciaService.registrar(dto);

        assertNotNull(resultado);
        assertEquals("ALTA", resultado.getSeveridad());
        assertEquals("ABIERTA", resultado.getEstado());
        verify(incidenciaRepository).save(any(Incidencia.class));
    }

    @Test
    @DisplayName("Registrar incidencia con estudiante inexistente lanza excepción")
    void registrar_estudiante_inexistente_lanza_excepcion() {
        IncidenciaRequestDTO dto = buildDTO("MEDIA");
        when(estudianteRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> incidenciaService.registrar(dto));
        verify(incidenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar incidencia con severidad inválida lanza ReglaNegocioException")
    void registrar_severidad_invalida_lanza_excepcion() {
        IncidenciaRequestDTO dto = buildDTO("EXTREMA"); // no es válida

        when(estudianteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(estudianteActivo));

        assertThrows(ReglaNegocioException.class,
                () -> incidenciaService.registrar(dto));
        verify(incidenciaRepository, never()).save(any());
    }

    // ─── Historial ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Historial por estudiante incluye incidencias cerradas")
    void historial_incluye_incidencias_cerradas() {
        Incidencia cerrada = new Incidencia();
        cerrada.setId(2L);
        cerrada.setEstudiante(estudianteActivo);
        cerrada.setTitulo("Incidencia resuelta");
        cerrada.setDescripcion("Resuelto por tutor.");
        cerrada.setSeveridad("BAJA");
        cerrada.setEstado("CERRADA");
        cerrada.setFechaIncidencia(LocalDateTime.now().minusDays(10));
        cerrada.setActivo(true);
        cerrada.setFechaAlta(LocalDateTime.now().minusDays(10));

        when(estudianteRepository.existsById(1L)).thenReturn(true);
        when(incidenciaRepository.findByEstudianteIdOrderByFechaIncidenciaDesc(1L))
                .thenReturn(List.of(incidenciaExistente, cerrada));

        List<IncidenciaResponseDTO> historial =
                incidenciaService.historialPorEstudiante(1L);

        assertEquals(2, historial.size());
        assertTrue(historial.stream()
                .anyMatch(i -> i.getEstado().equals("CERRADA")));
    }

    @Test
    @DisplayName("Historial de estudiante inexistente lanza excepción")
    void historial_estudiante_inexistente_lanza_excepcion() {
        when(estudianteRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class,
                () -> incidenciaService.historialPorEstudiante(99L));
    }

    // ─── Cambio de estado ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Cambiar estado a RESUELTA registra resolución y fecha de cierre")
    void cambiar_estado_resuelta_registra_cierre() {
        when(incidenciaRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(incidenciaExistente));
        when(incidenciaRepository.save(any())).thenReturn(incidenciaExistente);

        IncidenciaResponseDTO resultado = incidenciaService.cambiarEstado(
                1L, "RESUELTA", "Se habló con el estudiante y se normalizó la asistencia.");

        verify(incidenciaRepository).save(argThat(i ->
                i.getEstado().equals("RESUELTA")
                && i.getFechaCierre() != null
                && i.getResolucion() != null));
    }

    @Test
    @DisplayName("Cambiar estado con valor inválido lanza ReglaNegocioException")
    void cambiar_estado_invalido_lanza_excepcion() {
        assertThrows(ReglaNegocioException.class,
                () -> incidenciaService.cambiarEstado(1L, "ELIMINADA", null));
        verify(incidenciaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cambiar estado de incidencia inexistente lanza excepción")
    void cambiar_estado_inexistente_lanza_excepcion() {
        when(incidenciaRepository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> incidenciaService.cambiarEstado(99L, "RESUELTA", null));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private IncidenciaRequestDTO buildDTO(String severidad) {
        IncidenciaRequestDTO dto = new IncidenciaRequestDTO();
        dto.setEstudianteId(1L);
        dto.setTitulo("Falta injustificada reiterada");
        dto.setDescripcion("El estudiante acumula 3 faltas consecutivas.");
        dto.setSeveridad(severidad);
        dto.setTipo("Asistencia");
        return dto;
    }
}
