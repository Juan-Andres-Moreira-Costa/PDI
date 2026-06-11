package com.utec.sienep.service;

import com.utec.sienep.dto.request.BajaEstudianteRequestDTO;
import com.utec.sienep.dto.request.EstudianteRequestDTO;
import com.utec.sienep.dto.response.EstudianteResponseDTO;
import com.utec.sienep.entity.Estudiante;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.EstudianteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private EstudianteService estudianteService;

    private EstudianteRequestDTO dtoValido;
    private Estudiante estudianteExistente;

    // Cédula uruguaya real con dígito verificador correcto
    private static final String CEDULA_VALIDA = "22613196";
    private static final LocalDate FECHA_MAYOR = LocalDate.now().minusYears(20);

    @BeforeEach
    void setUp() {

        var auth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        dtoValido = new EstudianteRequestDTO();
        dtoValido.setCedula(CEDULA_VALIDA);
        dtoValido.setNombre("Juan");
        dtoValido.setApellido("Pérez");
        dtoValido.setEmail("juan@test.com");
        dtoValido.setFechaNacimiento(FECHA_MAYOR);

        dtoValido.setTelefono("099123456");
        dtoValido.setDireccion("Melo");
        dtoValido.setItr("ITR Centro Sur");
        dtoValido.setCarrera("Licenciatura en TI");
        dtoValido.setGrupo("A1");

        estudianteExistente = new Estudiante();
        estudianteExistente.setId(1L);
        estudianteExistente.setCedula(CEDULA_VALIDA);
        estudianteExistente.setNombre("Juan");
        estudianteExistente.setApellido("Pérez");
        estudianteExistente.setEmail("juan@test.com");
        estudianteExistente.setFechaNacimiento(FECHA_MAYOR);

        estudianteExistente.setTelefono("099123456");
        estudianteExistente.setDireccion("Melo");
        estudianteExistente.setItr("ITR Norte");
        estudianteExistente.setCarrera("Licenciatura en TI");
        estudianteExistente.setGrupo("Melo");

        estudianteExistente.setActivo(true);
        estudianteExistente.setFechaAlta(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        // Limpiar el contexto después de cada test para no contaminar otros
        SecurityContextHolder.clearContext();
    }

    // Alta

    @Test
    @DisplayName("Crear estudiante con datos válidos debe retornar DTO")
    void crear_estudiante_valido_retorna_dto() {
        when(estudianteRepository.existsByCedula(anyString())).thenReturn(false);
        when(estudianteRepository.existsByEmail(anyString())).thenReturn(false);
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudianteExistente);

        EstudianteResponseDTO resultado = estudianteService.crear(dtoValido);

        assertNotNull(resultado);
        assertEquals(CEDULA_VALIDA, resultado.getCedula());
        verify(estudianteRepository, times(1)).save(any(Estudiante.class));
    }

    @Test
    @DisplayName("Crear estudiante con cédula inválida debe lanzar ReglaNegocioException")
    void crear_cedula_invalida_lanza_excepcion() {
        dtoValido.setCedula("12345671"); // dígito verificador incorrecto

        assertThrows(ReglaNegocioException.class, () -> estudianteService.crear(dtoValido));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear estudiante menor de 18 años debe lanzar ReglaNegocioException")
    void crear_menor_de_edad_lanza_excepcion() {
        dtoValido.setFechaNacimiento(LocalDate.now().minusYears(16));

        assertThrows(ReglaNegocioException.class, () -> estudianteService.crear(dtoValido));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear estudiante con cédula duplicada debe lanzar ReglaNegocioException")
    void crear_cedula_duplicada_lanza_excepcion() {
        when(estudianteRepository.existsByCedula(anyString())).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> estudianteService.crear(dtoValido));
        verify(estudianteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear estudiante con email duplicado debe lanzar ReglaNegocioException")
    void crear_email_duplicado_lanza_excepcion() {
        when(estudianteRepository.existsByCedula(anyString())).thenReturn(false);
        when(estudianteRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> estudianteService.crear(dtoValido));
        verify(estudianteRepository, never()).save(any());
    }

    // Búsqueda

    @Test
    @DisplayName("Listar activos debe retornar estudiantes activos")
    void listar_activos_retorna_lista() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Estudiante> page =
                new PageImpl<>(List.of(estudianteExistente));

        when(estudianteRepository.findByActivoTrue(any(Pageable.class)))
                .thenReturn(page);

        Page<EstudianteResponseDTO> resultado =
                estudianteService.listarActivos(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Buscar por ID existente debe retornar el estudiante")
    void buscar_por_id_existente_retorna_dto() {
        when(estudianteRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(estudianteExistente));

        EstudianteResponseDTO resultado = estudianteService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("Buscar por ID inexistente debe lanzar RecursoNoEncontradoException")
    void buscar_por_id_inexistente_lanza_excepcion() {
        when(estudianteRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> estudianteService.buscarPorId(99L));
    }

    // Baja Lógica

    @Test
    @DisplayName("Dar de baja un estudiante activo debe marcarlo como inactivo")
    void dar_de_baja_activo_lo_desactiva() {
        when(estudianteRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(estudianteExistente));
        when(estudianteRepository.save(any(Estudiante.class))).thenReturn(estudianteExistente);

        BajaEstudianteRequestDTO bajaDto = new BajaEstudianteRequestDTO();
        bajaDto.setMotivoBaja("Egresado");

        assertDoesNotThrow(() -> estudianteService.darDeBaja(1L, bajaDto));
        verify(estudianteRepository, times(1)).save(argThat(e -> !e.isActivo()));
    }

    @Test
    @DisplayName("Dar de baja un estudiante inexistente debe lanzar excepción")
    void dar_de_baja_inexistente_lanza_excepcion() {
        when(estudianteRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        BajaEstudianteRequestDTO bajaDto = new BajaEstudianteRequestDTO();
        bajaDto.setMotivoBaja("Test");

        assertThrows(RecursoNoEncontradoException.class, () -> estudianteService.darDeBaja(99L, bajaDto));
    }
}
