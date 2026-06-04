package com.utec.sienep.service;

import com.utec.sienep.dto.request.InstanciaRequestDTO;
import com.utec.sienep.dto.response.InstanciaResponseDTO;
import com.utec.sienep.entity.*;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstanciaService {

    private final InstanciaRepository instanciaRepository;
    private final EstudianteRepository estudianteRepository;
    private final CategoriaInstanciaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public InstanciaService(InstanciaRepository instanciaRepository,
                            EstudianteRepository estudianteRepository,
                            CategoriaInstanciaRepository categoriaRepository,
                            UsuarioRepository usuarioRepository,
                            AuditoriaService auditoriaService) {
        this.instanciaRepository = instanciaRepository;
        this.estudianteRepository = estudianteRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    // ===================== RF10 – Registro de Instancias =====================

    @Transactional
    public InstanciaResponseDTO crear(InstanciaRequestDTO dto) {
        Estudiante estudiante = estudianteRepository.findByIdAndActivoTrue(dto.getEstudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un estudiante activo con ID: " + dto.getEstudianteId()));

        Instancia instancia = new Instancia();
        instancia.setEstudiante(estudiante);
        instancia.setIdentificador(generarIdentificador()); // RF14
        instancia.setTitulo(dto.getTitulo());
        instancia.setDescripcion(dto.getDescripcion());
        instancia.setFechaInstancia(dto.getFechaInstancia());
        instancia.setDuracionMinutos(dto.getDuracionMinutos());
        instancia.setLugar(dto.getLugar());
        instancia.setEstado("PROGRAMADA");
        instancia.setActivo(true);
        instancia.setFechaAlta(LocalDateTime.now());

        // RF11 – Categorizar instancia
        if (dto.getCategoriaId() != null) {
            CategoriaInstancia cat = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Categoría no encontrada: " + dto.getCategoriaId()));
            instancia.setCategoria(cat);
        }

        // Asignar usuario que la crea
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        usuarioRepository.findByUsernameAndActivoTrue(username)
                .ifPresent(instancia::setCreadoPor);

        Instancia guardada = instanciaRepository.save(instancia);

        auditoriaService.registrarExitoso(username, "ALTA_INSTANCIA",
                "Instancia", guardada.getId(),
                "Instancia " + guardada.getIdentificador() + " creada para estudiante ID " + dto.getEstudianteId());

        return mapearEntidadADto(guardada);
    }

    // ===================== RF12 – Visualización de Instancias =====================

    @Transactional(readOnly = true)
    public List<InstanciaResponseDTO> listarTodas() {
        return instanciaRepository.findByActivoTrueOrderByFechaInstanciaDesc()
                .stream().map(this::mapearEntidadADto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InstanciaResponseDTO> listarPorEstudiante(Long estudianteId) {
        if (!estudianteRepository.existsById(estudianteId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el estudiante con ID: " + estudianteId);
        }
        return instanciaRepository
                .findByEstudianteIdAndActivoTrueOrderByFechaInstanciaDesc(estudianteId)
                .stream().map(this::mapearEntidadADto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InstanciaResponseDTO buscarPorId(Long id) {
        return mapearEntidadADto(
                instanciaRepository.findByIdAndActivoTrue(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Instancia no encontrada con ID: " + id)));
    }

    // ===================== RF16 – Gestión de Instancias (modificar) =====================

    @Transactional
    public InstanciaResponseDTO modificar(Long id, InstanciaRequestDTO dto) {
        Instancia instancia = instanciaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Instancia no encontrada con ID: " + id));

        instancia.setTitulo(dto.getTitulo());
        instancia.setDescripcion(dto.getDescripcion());
        instancia.setFechaInstancia(dto.getFechaInstancia());
        instancia.setDuracionMinutos(dto.getDuracionMinutos());
        instancia.setLugar(dto.getLugar());
        instancia.setFechaModificacion(LocalDateTime.now());

        if (dto.getCategoriaId() != null) {
            CategoriaInstancia cat = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Categoría no encontrada: " + dto.getCategoriaId()));
            instancia.setCategoria(cat);
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrarExitoso(username, "MODIFICACION_INSTANCIA",
                "Instancia", id, "Instancia " + instancia.getIdentificador() + " modificada");

        return mapearEntidadADto(instanciaRepository.save(instancia));
    }

    // ===================== RF16 – Baja lógica de instancia =====================

    @Transactional
    public void darDeBaja(Long id) {
        Instancia instancia = instanciaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Instancia no encontrada con ID: " + id));

        instancia.setActivo(false);
        instancia.setEstado("CANCELADA");
        instanciaRepository.save(instancia);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        auditoriaService.registrarExitoso(username, "BAJA_INSTANCIA",
                "Instancia", id, "Instancia " + instancia.getIdentificador() + " cancelada");
    }

    // ===================== RF17 – Clonación de Instancias =====================

    @Transactional
    public InstanciaResponseDTO clonar(Long id, LocalDateTime nuevaFecha) {
        Instancia origen = instanciaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Instancia origen no encontrada con ID: " + id));

        Instancia clon = new Instancia();
        clon.setEstudiante(origen.getEstudiante());
        clon.setCategoria(origen.getCategoria());
        clon.setTitulo(origen.getTitulo());
        clon.setDescripcion(origen.getDescripcion());
        clon.setFechaInstancia(nuevaFecha != null ? nuevaFecha : origen.getFechaInstancia().plusWeeks(1));
        clon.setDuracionMinutos(origen.getDuracionMinutos());
        clon.setLugar(origen.getLugar());
        clon.setEstado("PROGRAMADA");
        clon.setActivo(true);
        clon.setFechaAlta(LocalDateTime.now());
        clon.setIdentificador(generarIdentificador());
        clon.setInstanciaOrigen(origen);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        usuarioRepository.findByUsernameAndActivoTrue(username).ifPresent(clon::setCreadoPor);

        Instancia guardado = instanciaRepository.save(clon);

        auditoriaService.registrarExitoso(username, "CLONAR_INSTANCIA",
                "Instancia", guardado.getId(),
                "Clonada desde " + origen.getIdentificador() + " → " + guardado.getIdentificador());

        return mapearEntidadADto(guardado);
    }

    // ===================== RF14 – Generación de Identificador =====================
    // Formato: INST-YYYYMMDD-XXXX (ej: INST-20260530-0001)

    private String generarIdentificador() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefijo = "INST-" + fecha + "-";
        long count = instanciaRepository.countByIdentificadorStartingWith(prefijo);
        return prefijo + String.format("%04d", count + 1);
    }

    // ===================== Mapeo entidad → DTO =====================

    private InstanciaResponseDTO mapearEntidadADto(Instancia i) {
        InstanciaResponseDTO dto = new InstanciaResponseDTO();
        dto.setId(i.getId());
        dto.setIdentificador(i.getIdentificador());
        dto.setEstudianteId(i.getEstudiante().getId());
        dto.setEstudianteNombre(i.getEstudiante().getNombre() + " " + i.getEstudiante().getApellido());
        dto.setEstudianteCedula(i.getEstudiante().getCedula());
        if (i.getCategoria() != null) {
            dto.setCategoriaId(i.getCategoria().getId());
            dto.setCategoriaNombre(i.getCategoria().getNombre());
        }
        dto.setTitulo(i.getTitulo());
        dto.setDescripcion(i.getDescripcion());
        dto.setFechaInstancia(i.getFechaInstancia());
        dto.setDuracionMinutos(i.getDuracionMinutos());
        dto.setLugar(i.getLugar());
        dto.setEstado(i.getEstado());
        dto.setGoogleCalendarEventId(i.getGoogleCalendarEventId());
        dto.setActivo(i.isActivo());
        dto.setFechaAlta(i.getFechaAlta());
        dto.setFechaModificacion(i.getFechaModificacion());
        if (i.getCreadoPor() != null) dto.setCreadoPorUsername(i.getCreadoPor().getUsername());
        if (i.getInstanciaOrigen() != null) dto.setInstanciaOrigenId(i.getInstanciaOrigen().getId());
        return dto;
    }
}
