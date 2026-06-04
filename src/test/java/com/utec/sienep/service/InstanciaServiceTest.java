package com.utec.sienep.service;

import com.utec.sienep.dto.request.InstanciaRequestDTO;
import com.utec.sienep.dto.response.InstanciaResponseDTO;
import com.utec.sienep.entity.*;
import com.utec.sienep.exception.RecursoNoEncontradoException;
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
@DisplayName("Tests de InstanciaService")
class InstanciaServiceTest {

    @Mock private InstanciaRepository instanciaRepository;
    @Mock private EstudianteRepository estudianteRepository;
    @Mock private CategoriaInstanciaRepository categoriaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private InstanciaService instanciaService;

    private Estudiante estudianteActivo;
    private Instancia instanciaExistente;

    @BeforeEach
    void setUp() {
        // Mock SecurityContext para que getUsername() funcione
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

        instanciaExistente = new Instancia();
        instanciaExistente.setId(1L);
        instanciaExistente.setIdentificador("INST-20260530-0001");
        instanciaExistente.setEstudiante(estudianteActivo);
        instanciaExistente.setTitulo("Tutoría inicial");
        instanciaExistente.setFechaInstancia(LocalDateTime.now().plusDays(3));
        instanciaExistente.setEstado("PROGRAMADA");
        instanciaExistente.setActivo(true);
        instanciaExistente.setFechaAlta(LocalDateTime.now());
    }

    // ===================== Creación =====================

    @Test
    @DisplayName("Crear instancia válida retorna DTO con identificador generado")
    void crear_instancia_valida_retorna_dto() {
        InstanciaRequestDTO dto = new InstanciaRequestDTO();
        dto.setEstudianteId(1L);
        dto.setTitulo("Tutoría inicial");
        dto.setFechaInstancia(LocalDateTime.now().plusDays(3));

        when(estudianteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(estudianteActivo));
        when(instanciaRepository.countByIdentificadorStartingWith(any())).thenReturn(0L);
        when(usuarioRepository.findByUsernameAndActivoTrue("admin"))
                .thenReturn(Optional.empty());
        when(instanciaRepository.save(any(Instancia.class)))
                .thenReturn(instanciaExistente);

        InstanciaResponseDTO resultado = instanciaService.crear(dto);

        assertNotNull(resultado);
        assertEquals("INST-20260530-0001", resultado.getIdentificador());
        verify(instanciaRepository).save(any(Instancia.class));
    }

    @Test
    @DisplayName("Crear instancia con estudiante inexistente lanza excepción")
    void crear_estudiante_inexistente_lanza_excepcion() {
        InstanciaRequestDTO dto = new InstanciaRequestDTO();
        dto.setEstudianteId(99L);
        dto.setTitulo("Test");
        dto.setFechaInstancia(LocalDateTime.now().plusDays(1));

        when(estudianteRepository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> instanciaService.crear(dto));
        verify(instanciaRepository, never()).save(any());
    }

    // ===================== Listado =====================

    @Test
    @DisplayName("Listar instancias retorna lista correcta")
    void listar_todas_retorna_lista() {
        when(instanciaRepository.findByActivoTrueOrderByFechaInstanciaDesc())
                .thenReturn(List.of(instanciaExistente));

        List<InstanciaResponseDTO> lista = instanciaService.listarTodas();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals("INST-20260530-0001", lista.get(0).getIdentificador());
    }

    @Test
    @DisplayName("Buscar instancia por ID inexistente lanza excepción")
    void buscar_por_id_inexistente_lanza_excepcion() {
        when(instanciaRepository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> instanciaService.buscarPorId(99L));
    }

    // ===================== Baja =====================

    @Test
    @DisplayName("Dar de baja instancia activa la cancela correctamente")
    void dar_de_baja_instancia_activa() {
        when(instanciaRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(instanciaExistente));
        when(instanciaRepository.save(any())).thenReturn(instanciaExistente);

        assertDoesNotThrow(() -> instanciaService.darDeBaja(1L));
        verify(instanciaRepository).save(argThat(i ->
                !i.isActivo() && i.getEstado().equals("CANCELADA")));
    }

    // ===================== Clonación =====================

    @Test
    @DisplayName("Clonar instancia crea una nueva con referencia a la original")
    void clonar_instancia_crea_nueva_con_origen() {
        Instancia clon = new Instancia();
        clon.setId(2L);
        clon.setIdentificador("INST-20260530-0002");
        clon.setEstudiante(estudianteActivo);
        clon.setTitulo("Tutoría inicial");
        clon.setFechaInstancia(LocalDateTime.now().plusWeeks(1));
        clon.setEstado("PROGRAMADA");
        clon.setActivo(true);
        clon.setFechaAlta(LocalDateTime.now());
        clon.setInstanciaOrigen(instanciaExistente);

        when(instanciaRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(instanciaExistente));
        when(instanciaRepository.countByIdentificadorStartingWith(any())).thenReturn(1L);
        when(usuarioRepository.findByUsernameAndActivoTrue("admin"))
                .thenReturn(Optional.empty());
        when(instanciaRepository.save(any())).thenReturn(clon);

        InstanciaResponseDTO resultado = instanciaService.clonar(1L, null);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getInstanciaOrigenId());
    }
}
