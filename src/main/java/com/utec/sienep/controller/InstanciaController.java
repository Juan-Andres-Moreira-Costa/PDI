package com.utec.sienep.controller;

import com.utec.sienep.dto.request.InstanciaRequestDTO;
import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.dto.response.InstanciaResponseDTO;
import com.utec.sienep.service.InstanciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/instancias")
@Tag(name = "Instancias", description = "Gestión de instancias del sistema SIENEP")
@SecurityRequirement(name = "bearerAuth")
public class InstanciaController {

    private final InstanciaService instanciaService;

    public InstanciaController(InstanciaService instanciaService) {
        this.instanciaService = instanciaService;
    }

    @PostMapping
    @Operation(summary = "Registrar instancia",
            description = "Crea una nueva instancia asociada a un estudiante. Genera automáticamente el identificador")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<InstanciaResponseDTO>> crear(
            @Valid @RequestBody InstanciaRequestDTO dto) {
        InstanciaResponseDTO creada = instanciaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Instancia registrada exitosamente.", creada));
    }

    @GetMapping
    @Operation(summary = "Listar instancias",
            description = "Lista todas las instancias activas. Filtro opcional por estudiante.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','PSICOPEDAGOGO','TUTOR','DIRECCION')")
    public ResponseEntity<ApiResponseDTO<List<InstanciaResponseDTO>>> listar(
            @RequestParam(required = false) Long estudianteId) {
        List<InstanciaResponseDTO> lista = estudianteId != null
                ? instanciaService.listarPorEstudiante(estudianteId)
                : instanciaService.listarTodas();
        return ResponseEntity.ok(ApiResponseDTO.ok("Listado de instancias.", lista));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar instancia por ID")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','PSICOPEDAGOGO','TUTOR','DIRECCION')")
    public ResponseEntity<ApiResponseDTO<InstanciaResponseDTO>> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Instancia encontrada.", instanciaService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar instancia")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<InstanciaResponseDTO>> modificar(
            @PathVariable Long id,
            @Valid @RequestBody InstanciaRequestDTO dto) {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Instancia modificada.", instanciaService.modificar(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar instancia — baja lógica")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<Void>> darDeBaja(@PathVariable Long id) {
        instanciaService.darDeBaja(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Instancia cancelada."));
    }

    @PostMapping("/{id}/clonar")
    @Operation(summary = "Clonar instancia",
            description = "Crea una copia de la instancia. Se puede indicar una nueva fecha; si no se indica, se programa una semana después.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<InstanciaResponseDTO>> clonar(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime nuevaFecha) {
        InstanciaResponseDTO clonada = instanciaService.clonar(id, nuevaFecha);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Instancia clonada exitosamente.", clonada));
    }

    // Nueva instancia desde ficha de alumno
    @PostMapping("/desde-estudiante/{estudianteId}")
    @Operation(summary = "Nueva instancia desde ficha de alumno",
            description = "Crea una instancia directamente desde la ficha del estudiante. " +
                    "El estudianteId se toma del path y se completa el resto del body.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<InstanciaResponseDTO>> crearDesdeEstudiante(
            @PathVariable Long estudianteId,
            @Valid @RequestBody InstanciaRequestDTO dto) {
        dto.setEstudianteId(estudianteId);
        InstanciaResponseDTO creada = instanciaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Instancia creada desde ficha del estudiante.", creada));
    }
}
