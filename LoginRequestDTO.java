package com.utec.sienep.service;

import com.utec.sienep.dto.request.BajaEstudianteRequestDTO;
import com.utec.sienep.dto.request.EstudianteRequestDTO;
import com.utec.sienep.dto.response.EstudianteResponseDTO;
import com.utec.sienep.entity.Estudiante;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.EstudianteRepository;
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
@DisplayName("Tests de EstudianteService")
class EstudianteServiceTest {

    @Mock
    private EstudianteRepository estudianteRepository;

    // Necesario porque EstudianteService recibe AuditoriaService en su constructor
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private EstudianteService estudianteService;

    private EstudianteRequestDTO dtoValido;
    private Estudiante estudianteExistente;

    // Cédula uruguaya válida con dígito verificador correcto
    private static final String CEDULA_VALIDA = "12345670";
    private static final LocalDate FECHA_MAYOR = LocalDate.now().minusYears(20);

    @BeforeEach
    void setUp() {
        // Mock del SecurityContext para que getUsername() no falle
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        when(auth.getAuthorities()).thenReturn(List.of());
        SecurityContext secCtx = mock(SecurityContext.class);
        when(secCtx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(secCtx);

        dtoValido = new EstudianteRequestDTO();
        dtoValido.setCedula(CEDULA_VALIDA);
        dtoValido.setNombre("Juan");
        dtoValido.setApellido("Pérez");
        dtoValido.setEmail("juan.perez@utec.edu.uy");
        dtoValido.setFechaNacimiento(FECHA_MAYOR);
        dtoValido.setItr("ITR Centro Sur");
        dtoValido.setCarrera("Ingeniería en Informática");

        estudianteExistente = new Estudiante();
        estudianteExistente.setId(1L);
        estudianteExistente.setCedula(CEDULA_VALIDA);
        estudianteExistente.setNombre("Juan");
        estudianteExistente.setApellido("Pérez");
        estudianteExistente.setEmail("juan.perez@utec.edu.uy");
        estudianteExistente.setFechaNacimiento(FECHA_MAYOR);
        estudianteExistente.setActivo(true);
        estudianteExistente.setFechaAlta(LocalDateTime.now());
    }

    // ===================== Alta (RF05) =====================

    @Test
    @DisplayName("Crear estudiante con datos válidos retorna DTO")
    void crear_estudiante_valido_retorna_dto() {
        when(estudianteRepository.existsByCedula(anyString())).thenReturn(false);
        when(estudianteRepository.existsByEmail(anyString())).thenReturn(false);
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudianteExistente);

        EstudianteResponseDTO resultado = estudianteService.crear(dtoValido);

        assertNotNull(resultado);
        assertEquals(CEDULA_VALIDA, resultado.getCedula());
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
        verify(auditoriaService, times(1)).registrarExitoso(
                anyString(), eq("ALTA_ESTUDIANTE"), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Crear estudiante con cédula inválida lanza ReglaNegocioException")
    void crear_cedula_invalida_lanza_excepcion() {
        dtoValido.setCedula("12345671");

        assertThrows(ReglaNegocioException.class, () -> estudianteService.crear(dtoValido));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear estudiante menor de 18 años lanza ReglaNegocioException")
    void crear_menor_de_edad_lanza_excepcion() {
        dtoValido.setFechaNacimiento(LocalDate.now().minusYears(16));

        assertThrows(ReglaNegocioException.class, () -> estudianteService.crear(dtoValido));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear estudiante con cédula duplicada lanza ReglaNegocioException")
    void crear_cedula_duplicada_lanza_excepcion() {
        when(estudianteRepository.existsByCedula(anyString())).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> estudianteService.crear(dtoValido));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear estudiante con email duplicado lanza ReglaNegocioException")
    void crear_email_duplicado_lanza_excepcion() {
        when(estudianteRepository.existsByCedula(anyString())).thenReturn(false);
        when(estudianteRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> estudianteService.crear(dtoValido));
        verify(estudianteRepository, never()).save(any());
    }

    // ===================== Búsqueda (RF08) =====================

    @Test
    @DisplayName("Listar activos retorna lista correcta")
    void listar_activos_retorna_lista() {
        when(estudianteRepository.findByActivoTrue()).thenReturn(List.of(estudianteExistente));

        List<EstudianteResponseDTO> lista = estudianteService.listarActivos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("Buscar por ID existente retorna el estudiante")
    void buscar_por_id_existente_retorna_dto() {
        when(estudianteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(estudianteExistente));

        EstudianteResponseDTO resultado = estudianteService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Buscar por ID inexistente lanza RecursoNoEncontradoException")
    void buscar_por_id_inexistente_lanza_excepcion() {
        when(estudianteRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> estudianteService.buscarPorId(99L));
    }

    // ===================== Baja lógica (RF06, RNF05) =====================

    @Test
    @DisplayName("Baja lógica de estudiante activo lo desactiva y registra auditoría")
    void dar_de_baja_activo_lo_desactiva() {
        when(estudianteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(estudianteExistente));
        when(estudianteRepository.save(any(Estudiante.class)))
                .thenReturn(estudianteExistente);

        BajaEstudianteRequestDTO bajaDto = new BajaEstudianteRequestDTO();
        bajaDto.setMotivoBaja("Egresado");

        assertDoesNotThrow(() -> estudianteService.darDeBaja(1L, bajaDto));
        verify(estudianteRepository).save(argThat(e ->
                !e.isActivo() && e.getMotivoBaja().equals("Egresado")));
        verify(auditoriaService).registrarExitoso(
                anyString(), eq("BAJA_ESTUDIANTE"), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Baja lógica de estudiante inexistente lanza excepción")
    void dar_de_baja_inexistente_lanza_excepcion() {
        when(estudianteRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        BajaEstudianteRequestDTO bajaDto = new BajaEstudianteRequestDTO();
        bajaDto.setMotivoBaja("Test");

        assertThrows(RecursoNoEncontradoException.class,
                () -> estudianteService.darDeBaja(99L, bajaDto));
    }
}
