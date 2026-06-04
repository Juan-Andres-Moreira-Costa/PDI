package com.utec.sienep.controller;

import com.utec.sienep.dto.request.RecordatorioRequestDTO;
import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.dto.response.InstanciaResponseDTO;
import com.utec.sienep.dto.response.RecordatorioResponseDTO;
import com.utec.sienep.service.RecordatorioService;
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
@RequestMapping("/api/v1/recordatorios")
@Tag(name = "Recordatorios", description = "Gestión de recordatorios del sistema SIENEP (RF19-RF27)")
@SecurityRequirement(name = "bearerAuth")
public class RecordatorioController {

    private final RecordatorioService recordatorioService;

    public RecordatorioController(RecordatorioService recordatorioService) {
        this.recordatorioService = recordatorioService;
    }

    // RF19 – Crear recordatorio
    @PostMapping
    @Operation(summary = "Crear recordatorio (RF19, RF20, RF21, RF24)",
        description = "Crea un recordatorio para un estudiante. " +
                      "Si esRecurrente=true, genera automáticamente la serie según la frecuencia indicada. " +
                      "El identificador se genera con formato REC-YYYYMMDD-XXXX.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<RecordatorioResponseDTO>> crear(
            @Valid @RequestBody RecordatorioRequestDTO dto) {
        RecordatorioResponseDTO creado = recordatorioService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Recordatorio creado exitosamente.", creado));
    }

    // RF26 – Listar / visualizar
    @GetMapping
    @Operation(summary = "Listar recordatorios (RF26)",
        description = "Lista todos los recordatorios activos. " +
                      "Filtro opcional por ?estudianteId= para ver los de un estudiante específico.")
    public ResponseEntity<ApiResponseDTO<List<RecordatorioResponseDTO>>> listar(
            @RequestParam(required = false) Long estudianteId) {
        List<RecordatorioResponseDTO> lista = estudianteId != null
                ? recordatorioService.listarPorEstudiante(estudianteId)
                : recordatorioService.listarTodos();
        return ResponseEntity.ok(ApiResponseDTO.ok("Listado de recordatorios.", lista));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar recordatorio por ID (RF26)")
    public ResponseEntity<ApiResponseDTO<RecordatorioResponseDTO>> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Recordatorio encontrado.", recordatorioService.buscarPorId(id)));
    }

    // RF25 – Gestión (modificar)
    @PutMapping("/{id}")
    @Operation(summary = "Modificar recordatorio (RF25)")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<RecordatorioResponseDTO>> modificar(
            @PathVariable Long id,
            @Valid @RequestBody RecordatorioRequestDTO dto) {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Recordatorio modificado.", recordatorioService.modificar(id, dto)));
    }

    // Baja lógica
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar recordatorio — baja lógica")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<Void>> darDeBaja(@PathVariable Long id) {
        recordatorioService.darDeBaja(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Recordatorio cancelado."));
    }

    // RF27 – Crear instancia desde recordatorio
    @PostMapping("/{id}/crear-instancia")
    @Operation(summary = "Crear instancia desde recordatorio (RF27)",
        description = "Genera una instancia a partir de los datos del recordatorio. " +
                      "El recordatorio queda en estado COMPLETADO y con referencia a la instancia creada.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<InstanciaResponseDTO>> crearInstancia(
            @PathVariable Long id) {
        InstanciaResponseDTO instancia = recordatorioService.crearInstanciaDesdeRecordatorio(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Instancia creada desde recordatorio.", instancia));
    }

    // RF23 – Procesar notificaciones pendientes (simulación)
    @PostMapping("/procesar-notificaciones")
    @Operation(summary = "Procesar notificaciones pendientes (RF23)",
        description = "Marca como notificados los recordatorios cuya fecha está en las próximas 24 horas. " +
                      "En producción esto se ejecutaría con @Scheduled automáticamente.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<RecordatorioResponseDTO>>> procesarNotificaciones() {
        List<RecordatorioResponseDTO> notificados =
                recordatorioService.procesarNotificacionesPendientes();
        return ResponseEntity.ok(
                ApiResponseDTO.ok(notificados.size() + " notificaciones procesadas.", notificados));
    }
}
