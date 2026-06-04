package com.utec.sienep.controller;

import com.utec.sienep.dto.request.IncidenciaRequestDTO;
import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.dto.response.IncidenciaResponseDTO;
import com.utec.sienep.service.IncidenciaService;
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
@RequestMapping("/api/v1/incidencias")
@Tag(name = "Incidencias", description = "Registro y seguimiento de incidencias (RF28-RF29)")
@SecurityRequirement(name = "bearerAuth")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    // RF28 – Registrar incidencia
    @PostMapping
    @Operation(summary = "Registrar incidencia (RF28)",
        description = "Registra una nueva incidencia para un estudiante. " +
                      "Puede vincularse a una instancia existente. " +
                      "Severidades: BAJA, MEDIA, ALTA, CRITICA.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<IncidenciaResponseDTO>> registrar(
            @Valid @RequestBody IncidenciaRequestDTO dto) {
        IncidenciaResponseDTO creada = incidenciaService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Incidencia registrada exitosamente.", creada));
    }

    // Listar todas
    @GetMapping
    @Operation(summary = "Listar incidencias activas")
    public ResponseEntity<ApiResponseDTO<List<IncidenciaResponseDTO>>> listar() {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Listado de incidencias.", incidenciaService.listarTodas()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar incidencia por ID")
    public ResponseEntity<ApiResponseDTO<IncidenciaResponseDTO>> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Incidencia encontrada.", incidenciaService.buscarPorId(id)));
    }

    // RF29 – Historial por estudiante (incluye cerradas)
    @GetMapping("/historial/{estudianteId}")
    @Operation(summary = "Historial de incidencias por estudiante (RF29)",
        description = "Retorna el historial completo de incidencias del estudiante, " +
                      "incluyendo las resueltas y cerradas. Garantiza trazabilidad histórica.")
    public ResponseEntity<ApiResponseDTO<List<IncidenciaResponseDTO>>> historial(
            @PathVariable Long estudianteId) {
        List<IncidenciaResponseDTO> historial =
                incidenciaService.historialPorEstudiante(estudianteId);
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Historial de incidencias del estudiante.", historial));
    }

    // Cambiar estado de la incidencia
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de incidencia",
        description = "Actualiza el estado de la incidencia. " +
                      "Estados válidos: ABIERTA, EN_PROCESO, RESUELTA, CERRADA. " +
                      "Si el estado es RESUELTA o CERRADA, registra la resolución y la fecha de cierre.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<IncidenciaResponseDTO>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false) String resolucion) {
        IncidenciaResponseDTO actualizada =
                incidenciaService.cambiarEstado(id, estado, resolucion);
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Estado de incidencia actualizado.", actualizada));
    }
}
