package com.utec.sienep.service;

import com.utec.sienep.dto.request.BajaEstudianteRequestDTO;
import com.utec.sienep.dto.request.EstudianteRequestDTO;
import com.utec.sienep.dto.response.EstudianteResponseDTO;
import com.utec.sienep.entity.Estudiante;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.EstudianteRepository;
import com.utec.sienep.util.ValidacionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    // ===================== RF05 – Alta de Estudiantes =====================

    @Transactional
    public EstudianteResponseDTO crear(EstudianteRequestDTO dto) {

        // Normalizar cédula (rellenar con cero si tiene 7 dígitos)
        String cedula = normalizarCedula(dto.getCedula());

        // Validar cédula uruguaya (regla de negocio obligatoria)
        if (!ValidacionUtil.validarCedulaUruguaya(cedula)) {
            throw new ReglaNegocioException(
                "La cédula '" + dto.getCedula() + "' no es válida según el algoritmo de dígito verificador uruguayo.");
        }

        // Validar edad mínima (regla de negocio obligatoria)
        if (!ValidacionUtil.esMayorDeEdad(dto.getFechaNacimiento())) {
            throw new ReglaNegocioException(
                "El estudiante debe ser mayor de 18 años para ser registrado.");
        }

        // Verificar que la cédula no esté ya registrada
        if (estudianteRepository.existsByCedula(cedula)) {
            throw new ReglaNegocioException(
                "Ya existe un estudiante registrado con la cédula: " + cedula);
        }

        // Verificar que el email no esté ya registrado
        if (estudianteRepository.existsByEmail(dto.getEmail())) {
            throw new ReglaNegocioException(
                "Ya existe un estudiante registrado con el email: " + dto.getEmail());
        }

        Estudiante estudiante = mapearDtoAEntidad(dto, new Estudiante());
        estudiante.setCedula(cedula);
        estudiante.setActivo(true);
        estudiante.setFechaAlta(LocalDateTime.now());

        Estudiante guardado = estudianteRepository.save(estudiante);
        return mapearEntidadADto(guardado);
    }

    // ===================== RF08 – Búsqueda de Estudiantes =====================

    @Transactional(readOnly = true)
    public List<EstudianteResponseDTO> listarActivos() {
        return estudianteRepository.findByActivoTrue()
                .stream()
                .map(this::mapearEntidadADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstudianteResponseDTO buscarPorId(Long id) {
        Estudiante estudiante = estudianteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un estudiante activo con ID: " + id));
        return mapearEntidadADto(estudiante);
    }

    @Transactional(readOnly = true)
    public EstudianteResponseDTO buscarPorCedula(String cedula) {
        String cedulaNorm = normalizarCedula(cedula);
        Estudiante estudiante = estudianteRepository.findByCedulaAndActivoTrue(cedulaNorm)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un estudiante activo con cédula: " + cedula));
        return mapearEntidadADto(estudiante);
    }

    @Transactional(readOnly = true)
    public List<EstudianteResponseDTO> buscarPorNombre(String termino) {
        return estudianteRepository.buscarPorNombreOApellido(termino)
                .stream()
                .map(this::mapearEntidadADto)
                .collect(Collectors.toList());
    }

    // ===================== RF07 – Modificación de Estudiantes =====================

    @Transactional
    public EstudianteResponseDTO modificar(Long id, EstudianteRequestDTO dto) {
        Estudiante estudiante = estudianteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un estudiante activo con ID: " + id));

        // Validar edad mínima con la nueva fecha (si cambió)
        if (!ValidacionUtil.esMayorDeEdad(dto.getFechaNacimiento())) {
            throw new ReglaNegocioException(
                "El estudiante debe ser mayor de 18 años.");
        }

        // Verificar email único (excluyendo el propio)
        if (!estudiante.getEmail().equalsIgnoreCase(dto.getEmail()) &&
                estudianteRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new ReglaNegocioException(
                "Ya existe otro estudiante registrado con el email: " + dto.getEmail());
        }

        // La cédula NO se puede cambiar una vez registrada
        mapearDtoAEntidad(dto, estudiante);
        estudiante.setFechaModificacion(LocalDateTime.now());

        Estudiante actualizado = estudianteRepository.save(estudiante);
        return mapearEntidadADto(actualizado);
    }

    // ===================== RF06 – Baja Lógica de Estudiantes =====================

    @Transactional
    public void darDeBaja(Long id, BajaEstudianteRequestDTO dto) {
        Estudiante estudiante = estudianteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró un estudiante activo con ID: " + id));

        estudiante.setActivo(false);
        estudiante.setFechaBaja(LocalDateTime.now());
        estudiante.setMotivoBaja(dto.getMotivoBaja());

        estudianteRepository.save(estudiante);
    }

    // ===================== Métodos auxiliares (mapeo DTO <-> Entidad) =====================

    private Estudiante mapearDtoAEntidad(EstudianteRequestDTO dto, Estudiante estudiante) {
        estudiante.setNombre(dto.getNombre().trim());
        estudiante.setApellido(dto.getApellido().trim());
        estudiante.setEmail(dto.getEmail().trim().toLowerCase());
        estudiante.setFechaNacimiento(dto.getFechaNacimiento());
        estudiante.setTelefono(dto.getTelefono());
        estudiante.setDireccion(dto.getDireccion());
        estudiante.setItr(dto.getItr());
        estudiante.setCarrera(dto.getCarrera());
        estudiante.setGrupo(dto.getGrupo());
        return estudiante;
    }

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
        dto.setActivo(e.isActivo());
        dto.setFechaAlta(e.getFechaAlta());
        dto.setFechaModificacion(e.getFechaModificacion());
        return dto;
    }

    private String normalizarCedula(String cedula) {
        if (cedula == null) return null;
        cedula = cedula.replaceAll("[.\\-]", "").trim();
        if (cedula.length() == 7) {
            cedula = "0" + cedula;
        }
        return cedula;
    }
}
