package com.utec.sienep.controller;

import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.entity.Auditoria;
import com.utec.sienep.repository.AuditoriaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
@Tag(name = "Auditoría", description = "Consulta del registro de auditoría y trazabilidad del sistema")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping
    @Operation(summary = "Listar todos los registros de auditoría",
        description = "Retorna todos los eventos auditados del sistema ordenados por fecha. " +
                      "Solo accesible para ROLE_ADMIN.")
    public ResponseEntity<ApiResponseDTO<List<Auditoria>>> listarTodos() {
        List<Auditoria> registros = auditoriaRepository
                .findAll(org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC, "fechaHora"));
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Registros de auditoría.", registros));
    }

    @GetMapping("/usuario/{username}")
    @Operation(summary = "Auditoría por usuario",
        description = "Retorna todos los eventos registrados para un username específico.")
    public ResponseEntity<ApiResponseDTO<List<Auditoria>>> porUsuario(
            @PathVariable String username) {
        List<Auditoria> registros =
                auditoriaRepository.findByUsernameOrderByFechaHoraDesc(username);
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Auditoría del usuario " + username + ".", registros));
    }

    @GetMapping("/entidad/{entidad}/{entidadId}")
    @Operation(summary = "Auditoría por entidad e ID",
        description = "Retorna el historial de operaciones sobre una entidad específica. " +
                      "Ejemplo: /auditoria/entidad/Estudiante/5")
    public ResponseEntity<ApiResponseDTO<List<Auditoria>>> porEntidad(
            @PathVariable String entidad,
            @PathVariable Long entidadId) {
        List<Auditoria> registros = auditoriaRepository
                .findByEntidadAndEntidadIdOrderByFechaHoraDesc(entidad, entidadId);
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Auditoría de " + entidad + " ID " + entidadId + ".", registros));
    }
}
