package com.utec.sienep.service;

import com.utec.sienep.dto.request.BajaEstudianteRequestDTO;
import com.utec.sienep.dto.request.EstudianteRequestDTO;
import com.utec.sienep.dto.response.EstudianteResponseDTO;
import com.utec.sienep.entity.Estudiante;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.EstudianteRepository;
import com.utec.sienep.util.ValidacionUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final AuditoriaService auditoriaService;

    public EstudianteService(EstudianteRepository estudianteRepository,
                             AuditoriaService auditoriaService) {
        this.estudianteRepository = estudianteRepository;
        this.auditoriaService = auditoriaService;
    }

    // ===================== RF05 – Alta =====================

    @Transactional
    public EstudianteResponseDTO crear(EstudianteRequestDTO dto) {
        String cedula = normalizarCedula(dto.getCedula());

        if (!ValidacionUtil.validarCedulaUruguaya(cedula)) {
            throw new ReglaNegocioException(
                    "La cédula '" + dto.getCedula() + "' no es válida según el algoritmo uruguayo.");
        }
        if (!ValidacionUtil.esMayorDeEdad(dto.getFechaNacimiento())) {
            throw new ReglaNegocioException(
                    "El estudiante debe ser mayor de 18 años para ser registrado.");
        }
        if (estudianteRepository.existsByCedula(cedula)) {
            throw new ReglaNegocioException(
                    "Ya existe un estudiante con la cédula: " + cedula);
        }
        if (estudianteRepository.existsByEmail(dto.getEmail())) {
            throw new ReglaNegocioException(
                    "Ya existe un estudiante con el email: " + dto.getEmail());
        }

        Estudiante e = mapearDtoAEntidad(dto, new Estudiante());
        e.setCedula(cedula);
        e.setActivo(true);
        e.setFechaAlta(LocalDateTime.now());

        Estudiante guardado = estudianteRepository.save(e);

        auditoriaService.registrarExitoso(getUsername(), "ALTA_ESTUDIANTE",
                "Estudiante", guardado.getId(), "Alta CI: " + cedula);

        return mapearEntidadADto(guardado);
    }

    // ===================== RF08 – Búsqueda =====================

    @Transactional(readOnly = true)
    public List<EstudianteResponseDTO> listarActivos() {
        return estudianteRepository.findByActivoTrue()
                .stream().map(this::mapearEntidadADto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstudianteResponseDTO buscarPorId(Long id) {
        return mapearEntidadADto(
                estudianteRepository.findByIdAndActivoTrue(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Estudiante no encontrado con ID: " + id)));
    }

    @Transactional(readOnly = true)
    public EstudianteResponseDTO buscarPorCedula(String cedula) {
        return mapearEntidadADto(
                estudianteRepository.findByCedulaAndActivoTrue(normalizarCedula(cedula))
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Estudiante no encontrado con cédula: " + cedula)));
    }

    @Transactional(readOnly = true)
    public List<EstudianteResponseDTO> buscarPorNombre(String termino) {
        return estudianteRepository.buscarPorNombreOApellido(termino)
                .stream().map(this::mapearEntidadADto).collect(Collectors.toList());
    }

    // ===================== RF07 – Modificación =====================

    @Transactional
    public EstudianteResponseDTO modificar(Long id, EstudianteRequestDTO dto) {
        Estudiante e = estudianteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Estudiante no encontrado con ID: " + id));

        if (!ValidacionUtil.esMayorDeEdad(dto.getFechaNacimiento())) {
            throw new ReglaNegocioException("El estudiante debe ser mayor de 18 años.");
        }
        if (!e.getEmail().equalsIgnoreCase(dto.getEmail()) &&
                estudianteRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new ReglaNegocioException("Ya existe otro estudiante con el email: " + dto.getEmail());
        }

        mapearDtoAEntidad(dto, e);
        e.setFechaModificacion(LocalDateTime.now());
        Estudiante actualizado = estudianteRepository.save(e);

        auditoriaService.registrarExitoso(getUsername(), "MODIFICACION_ESTUDIANTE",
                "Estudiante", id, "Modificación ID: " + id);

        return mapearEntidadADto(actualizado);
    }

    // ===================== RF06 – Baja lógica (RNF05) =====================

    @Transactional
    public void darDeBaja(Long id, BajaEstudianteRequestDTO dto) {
        Estudiante e = estudianteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Estudiante no encontrado con ID: " + id));

        e.setActivo(false);
        e.setFechaBaja(LocalDateTime.now());
        e.setMotivoBaja(dto.getMotivoBaja());
        estudianteRepository.save(e);

        auditoriaService.registrarExitoso(getUsername(), "BAJA_ESTUDIANTE",
                "Estudiante", id,
                "Baja lógica ID: " + id + " — Motivo: " + dto.getMotivoBaja());
    }

    // ===================== Mapeo DTO ↔ Entidad =====================

    /**
     * RNF01/RD01 — El campo observacionesConfidenciales solo se persiste
     * si el usuario tiene ROLE_DIRECCION o ROLE_ADMIN.
     * informacionSalud solo para ROLE_PSICOPEDAGOGO, ROLE_ADMIN, ROLE_DIRECCION.
     */
    private Estudiante mapearDtoAEntidad(EstudianteRequestDTO dto, Estudiante e) {
        e.setNombre(dto.getNombre().trim());
        e.setApellido(dto.getApellido().trim());
        e.setEmail(dto.getEmail().trim().toLowerCase());
        e.setFechaNacimiento(dto.getFechaNacimiento());
        e.setTelefono(dto.getTelefono());
        e.setDireccion(dto.getDireccion());
        e.setItr(dto.getItr());
        e.setCarrera(dto.getCarrera());
        e.setGrupo(dto.getGrupo());
        e.setSistemaSalud(dto.getSistemaSalud());
        e.setMotivoDerivacion(dto.getMotivoDerivacion());
        e.setObservaciones(dto.getObservaciones());

        // RNF01 — solo roles autorizados pueden guardar info de salud
        if (tieneRol("ROLE_PSICOPEDAGOGO", "ROLE_ADMIN", "ROLE_DIRECCION")) {
            e.setInformacionSalud(dto.getInformacionSalud());
        }
        // RD01 — solo ROLE_DIRECCION y ROLE_ADMIN guardan el campo confidencial
        if (tieneRol("ROLE_DIRECCION", "ROLE_ADMIN")) {
            e.setObservacionesConfidenciales(dto.getObservacionesConfidenciales());
        }

        return e;
    }

    /**
     * RNF01/RD01 — El DTO de respuesta omite campos sensibles según el rol del solicitante.
     * @JsonInclude(NON_NULL) en el DTO se encarga de no serializar los campos null.
     */
    private EstudianteResponseDTO mapearEntidadADto(Estudiante e) {
        EstudianteResponseDTO dto = new EstudianteResponseDTO();
        dto.setId(e.getId());
        dto.setCedula(e.getCedula());
        dto.setNombre(e.getNombre());
        dto.setApellido(e.getApellido());
        dto.setEmail(e.getEmail());
        dto.setFechaNacimiento(e.getFechaNacimiento());
        dto.setTelefono(e.getTelefono());
        dto.setDireccion(e.getDireccion());
        dto.setItr(e.getItr());
        dto.setCarrera(e.getCarrera());
        dto.setGrupo(e.getGrupo());
        dto.setSistemaSalud(e.getSistemaSalud());
        dto.setMotivoDerivacion(e.getMotivoDerivacion());
        dto.setObservaciones(e.getObservaciones());
        dto.setActivo(e.isActivo());
        dto.setFechaAlta(e.getFechaAlta());
        dto.setFechaModificacion(e.getFechaModificacion());

        // RNF01 — solo roles autorizados ven información de salud
        if (tieneRol("ROLE_PSICOPEDAGOGO", "ROLE_ADMIN", "ROLE_DIRECCION")) {
            dto.setInformacionSalud(e.getInformacionSalud());
        }
        // RD01 — solo ROLE_DIRECCION y ROLE_ADMIN ven el campo confidencial
        if (tieneRol("ROLE_DIRECCION", "ROLE_ADMIN")) {
            dto.setObservacionesConfidenciales(e.getObservacionesConfidenciales());
        }

        return dto;
    }

    // ===================== Helpers =====================

    private boolean tieneRol(String... roles) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return false;
            for (String rol : roles) {
                if (auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(rol))) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String getUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "sistema";
        }
    }

    private String normalizarCedula(String cedula) {
        if (cedula == null) return null;
        cedula = cedula.replaceAll("[.\\-]", "").trim();
        if (cedula.length() == 7) cedula = "0" + cedula;
        return cedula;
    }
}
