package com.utec.sienep.controller;

import com.utec.sienep.dto.request.InformeMedicoRequestDTO;
import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.dto.response.InformeMedicoResponseDTO;
import com.utec.sienep.service.InformeMedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estudiantes/{estudianteId}/informes-medicos")
@Tag(name = "Informes Médicos", description = "Gestión de informes médicos por estudiante (RF09)")
@SecurityRequirement(name = "bearerAuth")
public class InformeMedicoController {

    private final InformeMedicoService informeMedicoService;

    public InformeMedicoController(InformeMedicoService informeMedicoService) {
        this.informeMedicoService = informeMedicoService;
    }

    @PostMapping
    @Operation(summary = "Registrar informe médico (RF09)",
        description = "Adjunta un informe médico al estudiante. " +
                      "Solo ADMIN y DOCENTE pueden cargar informes. " +
                      "Los datos sensibles solo son visibles para roles autorizados.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<InformeMedicoResponseDTO>> registrar(
            @PathVariable Long estudianteId,
            @Valid @RequestBody InformeMedicoRequestDTO dto) {
        InformeMedicoResponseDTO creado = informeMedicoService.registrar(estudianteId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Informe médico registrado exitosamente.", creado));
    }

    @GetMapping
    @Operation(summary = "Listar informes médicos del estudiante (RF09)",
        description = "Retorna los informes médicos activos del estudiante. " +
                      "Solo accesible para ADMIN y DOCENTE — datos sensibles/confidenciales.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<List<InformeMedicoResponseDTO>>> listar(
            @PathVariable Long estudianteId) {
        List<InformeMedicoResponseDTO> lista =
                informeMedicoService.listarPorEstudiante(estudianteId);
        return ResponseEntity.ok(ApiResponseDTO.ok("Informes médicos del estudiante.", lista));
    }

    @DeleteMapping("/{informeId}")
    @Operation(summary = "Eliminar informe médico — baja lógica (RF09)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> eliminar(
            @PathVariable Long estudianteId,
            @PathVariable Long informeId) {
        informeMedicoService.eliminar(informeId);
        return ResponseEntity.ok(ApiResponseDTO.ok("Informe médico eliminado."));
    }
}
