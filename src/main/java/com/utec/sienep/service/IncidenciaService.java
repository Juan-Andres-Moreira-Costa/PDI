package com.utec.sienep.service;

import com.utec.sienep.dto.request.IncidenciaRequestDTO;
import com.utec.sienep.dto.response.IncidenciaResponseDTO;
import com.utec.sienep.entity.*;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final EstudianteRepository estudianteRepository;
    private final InstanciaRepository instanciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public IncidenciaService(IncidenciaRepository incidenciaRepository,
                             EstudianteRepository estudianteRepository,
                             InstanciaRepository instanciaRepository,
                             UsuarioRepository usuarioRepository,
                             AuditoriaService auditoriaService) {
        this.incidenciaRepository = incidenciaRepository;
        this.estudianteRepository = estudianteRepository;
        this.instanciaRepository = instanciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    // ===================== RF28 – Registro de Incidencias =====================

    @Transactional
    public IncidenciaResponseDTO registrar(IncidenciaRequestDTO dto) {
        Estudiante estudiante = estudianteRepository.findByIdAndActivoTrue(dto.getEstudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Estudiante no encontrado: " + dto.getEstudianteId()));

        validarSeveridad(dto.getSeveridad());

        Incidencia inc = new Incidencia();
        inc.setEstudiante(estudiante);
        inc.setTitulo(dto.getTitulo().trim());
        inc.setDescripcion(dto.getDescripcion().trim());
        inc.setTipo(dto.getTipo());
        inc.setSeveridad(dto.getSeveridad().toUpperCase());
        inc.setEstado("ABIERTA");
        inc.setFechaIncidencia(LocalDateTime.now());
        inc.setActivo(true);
        inc.setFechaAlta(LocalDateTime.now());

        // Vincular a una instancia si se proporciona
        if (dto.getInstanciaId() != null) {
            instanciaRepository.findByIdAndActivoTrue(dto.getInstanciaId())
                    .ifPresent(inc::setInstancia);
        }

        String username = getUsername();
        usuarioRepository.findByUsernameAndActivoTrue(username)
                .ifPresent(inc::setRegistradoPor);

        Incidencia guardada = incidenciaRepository.save(inc);

        auditoriaService.registrarExitoso(username, "ALTA_INCIDENCIA",
                "Incidencia", guardada.getId(),
                "Incidencia registrada para estudiante ID " + dto.getEstudianteId()
                + " — Severidad: " + dto.getSeveridad());

        return mapearEntidadADto(guardada);
    }

    // ===================== RF29 – Historial de Incidencias =====================

    @Transactional(readOnly = true)
    public List<IncidenciaResponseDTO> historialPorEstudiante(Long estudianteId) {
        if (!estudianteRepository.existsById(estudianteId)) {
            throw new RecursoNoEncontradoException(
                    "Estudiante no encontrado: " + estudianteId);
        }
        // Historial completo — incluyendo resueltas y cerradas (no solo activas)
        return incidenciaRepository
                .findByEstudianteIdOrderByFechaIncidenciaDesc(estudianteId)
                .stream().map(this::mapearEntidadADto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponseDTO> listarTodas() {
        return incidenciaRepository.findByActivoTrueOrderByFechaIncidenciaDesc()
                .stream().map(this::mapearEntidadADto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncidenciaResponseDTO buscarPorId(Long id) {
        return mapearEntidadADto(
                incidenciaRepository.findByIdAndActivoTrue(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Incidencia no encontrada: " + id)));
    }

    // ===================== Cambiar estado de incidencia =====================

    @Transactional
    public IncidenciaResponseDTO cambiarEstado(Long id, String nuevoEstado, String resolucion) {
        List<String> estadosValidos = List.of("ABIERTA", "EN_PROCESO", "RESUELTA", "CERRADA");
        if (!estadosValidos.contains(nuevoEstado.toUpperCase())) {
            throw new ReglaNegocioException(
                    "Estado inválido. Valores aceptados: " + estadosValidos);
        }

        Incidencia inc = incidenciaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Incidencia no encontrada: " + id));

        inc.setEstado(nuevoEstado.toUpperCase());
        inc.setFechaModificacion(LocalDateTime.now());

        if (nuevoEstado.equalsIgnoreCase("RESUELTA")
                || nuevoEstado.equalsIgnoreCase("CERRADA")) {
            inc.setFechaCierre(LocalDateTime.now());
            if (resolucion != null && !resolucion.isBlank()) {
                inc.setResolucion(resolucion.trim());
            }
        }

        Incidencia actualizada = incidenciaRepository.save(inc);

        auditoriaService.registrarExitoso(getUsername(), "CAMBIO_ESTADO_INCIDENCIA",
                "Incidencia", id,
                "Estado cambiado a " + nuevoEstado + " — ID: " + id);

        return mapearEntidadADto(actualizada);
    }

    // ===================== Helpers =====================

    private void validarSeveridad(String severidad) {
        List<String> validas = List.of("BAJA", "MEDIA", "ALTA", "CRITICA");
        if (severidad == null || !validas.contains(severidad.toUpperCase())) {
            throw new ReglaNegocioException(
                    "Severidad inválida. Valores aceptados: " + validas);
        }
    }

    private String getUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "sistema";
        }
    }

    private IncidenciaResponseDTO mapearEntidadADto(Incidencia i) {
        IncidenciaResponseDTO dto = new IncidenciaResponseDTO();
        dto.setId(i.getId());
        dto.setEstudianteId(i.getEstudiante().getId());
        dto.setEstudianteNombre(i.getEstudiante().getNombre() + " " + i.getEstudiante().getApellido());
        dto.setEstudianteCedula(i.getEstudiante().getCedula());
        if (i.getInstancia() != null) {
            dto.setInstanciaId(i.getInstancia().getId());
            dto.setInstanciaIdentificador(i.getInstancia().getIdentificador());
        }
        dto.setTitulo(i.getTitulo());
        dto.setDescripcion(i.getDescripcion());
        dto.setTipo(i.getTipo());
        dto.setSeveridad(i.getSeveridad());
        dto.setEstado(i.getEstado());
        dto.setFechaIncidencia(i.getFechaIncidencia());
        dto.setFechaCierre(i.getFechaCierre());
        dto.setResolucion(i.getResolucion());
        if (i.getRegistradoPor() != null) dto.setRegistradoPorUsername(i.getRegistradoPor().getUsername());
        dto.setActivo(i.isActivo());
        dto.setFechaAlta(i.getFechaAlta());
        dto.setFechaModificacion(i.getFechaModificacion());
        return dto;
    }
}
