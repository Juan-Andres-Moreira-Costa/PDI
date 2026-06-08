package com.utec.sienep.service;

import com.utec.sienep.dto.request.InformeMedicoRequestDTO;
import com.utec.sienep.dto.response.InformeMedicoResponseDTO;
import com.utec.sienep.entity.InformeMedico;
import com.utec.sienep.entity.Estudiante;
import com.utec.sienep.entity.Usuario;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.repository.EstudianteRepository;
import com.utec.sienep.repository.InformeMedicoRepository;
import com.utec.sienep.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InformeMedicoService {

    private final InformeMedicoRepository informeMedicoRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public InformeMedicoService(InformeMedicoRepository informeMedicoRepository,
                                EstudianteRepository estudianteRepository,
                                UsuarioRepository usuarioRepository,
                                AuditoriaService auditoriaService) {
        this.informeMedicoRepository = informeMedicoRepository;
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    // ===================== RF09 – Adjuntar Informe Médico =====================

    @Transactional
    public InformeMedicoResponseDTO registrar(Long estudianteId,
                                              InformeMedicoRequestDTO dto) {
        Estudiante estudiante = estudianteRepository.findByIdAndActivoTrue(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Estudiante no encontrado con ID: " + estudianteId));

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        InformeMedico informe = new InformeMedico();
        informe.setEstudiante(estudiante);
        informe.setNombreArchivo(dto.getNombreArchivo().trim());
        informe.setTipoArchivo(dto.getTipoArchivo());
        informe.setDescripcion(dto.getDescripcion());
        informe.setFechaCarga(LocalDateTime.now());
        informe.setActivo(true);

        usuarioRepository.findByUsernameAndActivoTrue(username)
                .ifPresent(informe::setCargadoPor);

        InformeMedico guardado = informeMedicoRepository.save(informe);

        auditoriaService.registrarExitoso(username, "ALTA_INFORME_MEDICO",
                "InformeMedico", guardado.getId(),
                "Informe '" + dto.getNombreArchivo() + "' registrado para estudiante ID " + estudianteId);

        return mapearEntidadADto(guardado);
    }

    // ===================== Listar informes por estudiante =====================

    @Transactional(readOnly = true)
    public List<InformeMedicoResponseDTO> listarPorEstudiante(Long estudianteId) {
        if (!estudianteRepository.existsById(estudianteId)) {
            throw new RecursoNoEncontradoException(
                    "Estudiante no encontrado con ID: " + estudianteId);
        }
        return informeMedicoRepository.findByEstudianteIdAndActivoTrue(estudianteId)
                .stream()
                .map(this::mapearEntidadADto)
                .collect(Collectors.toList());
    }

    // ===================== Baja lógica de informe =====================

    @Transactional
    public void eliminar(Long informeId) {
        InformeMedico informe = informeMedicoRepository.findById(informeId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Informe médico no encontrado con ID: " + informeId));

        informe.setActivo(false);
        informeMedicoRepository.save(informe);

        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        auditoriaService.registrarExitoso(username, "BAJA_INFORME_MEDICO",
                "InformeMedico", informeId, "Informe dado de baja lógica");
    }

    // ===================== Mapeo =====================

    private InformeMedicoResponseDTO mapearEntidadADto(InformeMedico i) {
        InformeMedicoResponseDTO dto = new InformeMedicoResponseDTO();
        dto.setId(i.getId());
        dto.setEstudianteId(i.getEstudiante().getId());
        dto.setEstudianteNombre(i.getEstudiante().getNombre()
                + " " + i.getEstudiante().getApellido());
        dto.setNombreArchivo(i.getNombreArchivo());
        dto.setTipoArchivo(i.getTipoArchivo());
        dto.setDescripcion(i.getDescripcion());
        dto.setFechaCarga(i.getFechaCarga());
        if (i.getCargadoPor() != null) {
            dto.setCargadoPorUsername(i.getCargadoPor().getUsername());
        }
        dto.setActivo(i.isActivo());
        return dto;
    }
}
