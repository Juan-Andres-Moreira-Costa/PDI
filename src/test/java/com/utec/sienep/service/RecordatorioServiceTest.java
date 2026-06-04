package com.utec.sienep.service;

import com.utec.sienep.dto.request.RecordatorioRequestDTO;
import com.utec.sienep.dto.response.RecordatorioResponseDTO;
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
@DisplayName("Tests de RecordatorioService")
class RecordatorioServiceTest {

    @Mock private RecordatorioRepository recordatorioRepository;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private CategoriaRecordatorioRepository categoriaRepository;
    @Mock private InstanciaRepository instanciaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuditoriaService auditoriaService;
    @Mock private InstanciaService instanciaService;

    @InjectMocks
    private RecordatorioService recordatorioService;

    private Estudiante estudianteActivo;
    private Recordatorio recordatorioExistente;

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

        recordatorioExistente = new Recordatorio();
        recordatorioExistente.setId(1L);
        recordatorioExistente.setIdentificador("REC-20260628-0001");
        recordatorioExistente.setEstudiante(estudianteActivo);
        recordatorioExistente.setTitulo("Entrega documentación");
        recordatorioExistente.setFechaRecordatorio(LocalDateTime.now().plusDays(5));
        recordatorioExistente.setTipo("ACADEMICO");
        recordatorioExistente.setEstado("PENDIENTE");
        recordatorioExistente.setActivo(true);
        recordatorioExistente.setFechaAlta(LocalDateTime.now());
    }

    // ─── Creación ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Crear recordatorio simple válido retorna DTO con identificador")
    void crear_recordatorio_simple_retorna_dto() {
        RecordatorioRequestDTO dto = buildDTO(false, null, null);

        when(estudianteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(estudianteActivo));
        when(recordatorioRepository.countByIdentificadorStartingWith(any())).thenReturn(0L);
        when(usuarioRepository.findByUsernameAndActivoTrue("admin"))
                .thenReturn(Optional.empty());
        when(recordatorioRepository.save(any(Recordatorio.class)))
                .thenReturn(recordatorioExistente);

        RecordatorioResponseDTO resultado = recordatorioService.crear(dto);

        assertNotNull(resultado);
        assertEquals("REC-20260628-0001", resultado.getIdentificador());
        verify(recordatorioRepository, atLeastOnce()).save(any(Recordatorio.class));
    }

    @Test
    @DisplayName("Crear recordatorio con estudiante inexistente lanza excepción")
    void crear_estudiante_inexistente_lanza_excepcion() {
        RecordatorioRequestDTO dto = buildDTO(false, null, null);
        when(estudianteRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> recordatorioService.crear(dto));
        verify(recordatorioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear recordatorio recurrente sin frecuencia lanza ReglaNegocioException")
    void crear_recurrente_sin_frecuencia_lanza_excepcion() {
        RecordatorioRequestDTO dto = buildDTO(true, null,
                LocalDateTime.now().plusMonths(3));

        when(estudianteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(estudianteActivo));

        assertThrows(ReglaNegocioException.class,
                () -> recordatorioService.crear(dto));
        verify(recordatorioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear recordatorio recurrente sin fecha fin lanza ReglaNegocioException")
    void crear_recurrente_sin_fecha_fin_lanza_excepcion() {
        RecordatorioRequestDTO dto = buildDTO(true, "SEMANAL", null);

        when(estudianteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(estudianteActivo));

        assertThrows(ReglaNegocioException.class,
                () -> recordatorioService.crear(dto));
        verify(recordatorioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear recordatorio recurrente con fecha fin anterior lanza excepción")
    void crear_recurrente_fecha_fin_anterior_lanza_excepcion() {
        RecordatorioRequestDTO dto = buildDTO(true, "SEMANAL",
                LocalDateTime.now().minusDays(1)); // fecha fin en el pasado

        when(estudianteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(estudianteActivo));

        assertThrows(ReglaNegocioException.class,
                () -> recordatorioService.crear(dto));
    }

    // ─── Listado ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Listar todos retorna lista correcta")
    void listar_todos_retorna_lista() {
        when(recordatorioRepository.findByActivoTrueOrderByFechaRecordatorioAsc())
                .thenReturn(List.of(recordatorioExistente));

        List<RecordatorioResponseDTO> lista = recordatorioService.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("REC-20260628-0001", lista.get(0).getIdentificador());
    }

    @Test
    @DisplayName("Buscar por ID inexistente lanza excepción")
    void buscar_por_id_inexistente_lanza_excepcion() {
        when(recordatorioRepository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> recordatorioService.buscarPorId(99L));
    }

    // ─── Baja ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Dar de baja recordatorio activo lo cancela")
    void dar_de_baja_cancela_recordatorio() {
        when(recordatorioRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(recordatorioExistente));
        when(recordatorioRepository.save(any())).thenReturn(recordatorioExistente);

        assertDoesNotThrow(() -> recordatorioService.darDeBaja(1L));
        verify(recordatorioRepository).save(argThat(r ->
                !r.isActivo() && r.getEstado().equals("CANCELADO")));
    }

    // ─── RF27 ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Crear instancia desde recordatorio ya vinculado lanza excepción")
    void crear_instancia_desde_recordatorio_ya_vinculado_lanza_excepcion() {
        Instancia instanciaExistente = new Instancia();
        instanciaExistente.setId(1L);
        instanciaExistente.setIdentificador("INST-20260628-0001");
        recordatorioExistente.setInstanciaGenerada(instanciaExistente);

        when(recordatorioRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(recordatorioExistente));

        assertThrows(ReglaNegocioException.class,
                () -> recordatorioService.crearInstanciaDesdeRecordatorio(1L));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private RecordatorioRequestDTO buildDTO(boolean recurrente,
                                            String frecuencia,
                                            LocalDateTime fechaFin) {
        RecordatorioRequestDTO dto = new RecordatorioRequestDTO();
        dto.setEstudianteId(1L);
        dto.setTitulo("Entrega documentación");
        dto.setFechaRecordatorio(LocalDateTime.now().plusDays(5));
        dto.setTipo("ACADEMICO");
        dto.setEsRecurrente(recurrente);
        dto.setFrecuenciaRecurrencia(frecuencia);
        dto.setFechaFinRecurrencia(fechaFin);
        return dto;
    }
}
