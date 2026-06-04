package com.utec.sienep.controller;

import com.utec.sienep.dto.request.BajaEstudianteRequestDTO;
import com.utec.sienep.dto.request.EstudianteRequestDTO;
import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.dto.response.EstudianteResponseDTO;
import com.utec.sienep.service.EstudianteService;
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
@RequestMapping("/api/v1/estudiantes")
@Tag(name = "Estudiantes", description = "Gestión completa de estudiantes (RF05-RF09). " +
        "RNF01/RD01: los campos sensibles (informacionSalud, observacionesConfidenciales) " +
        "solo aparecen en la respuesta según el rol del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @PostMapping
    @Operation(summary = "Alta de estudiante (RF05)",
        description = "Registra un nuevo estudiante. " +
                      "Valida cédula uruguaya (dígito verificador) y edad mínima 18 años. " +
                      "RD01: observacionesConfidenciales solo es persistido por ROLE_DIRECCION/ADMIN.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','PSICOPEDAGOGO')")
    public ResponseEntity<ApiResponseDTO<EstudianteResponseDTO>> crear(
            @Valid @RequestBody EstudianteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Estudiante registrado exitosamente.",
                        estudianteService.crear(dto)));
    }

    @GetMapping
    @Operation(summary = "Listar / buscar estudiantes activos (RF08)",
        description = "Filtros opcionales: ?nombre= (parcial), ?cedula= (exacta). " +
                      "RNF01: la respuesta incluye informacionSalud solo para roles autorizados.")
    public ResponseEntity<ApiResponseDTO<List<EstudianteResponseDTO>>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String cedula) {

        if (cedula != null && !cedula.isBlank()) {
            return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante encontrado.",
                    List.of(estudianteService.buscarPorCedula(cedula))));
        }
        if (nombre != null && !nombre.isBlank()) {
            return ResponseEntity.ok(ApiResponseDTO.ok("Resultados de búsqueda.",
                    estudianteService.buscarPorNombre(nombre)));
        }
        return ResponseEntity.ok(ApiResponseDTO.ok("Listado de estudiantes activos.",
                estudianteService.listarActivos()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar estudiante por ID (RF08)")
    public ResponseEntity<ApiResponseDTO<EstudianteResponseDTO>> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante encontrado.",
                estudianteService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar estudiante (RF07)",
        description = "Actualiza los datos del estudiante. La cédula no puede modificarse.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','PSICOPEDAGOGO')")
    public ResponseEntity<ApiResponseDTO<EstudianteResponseDTO>> modificar(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante modificado exitosamente.",
                estudianteService.modificar(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de estudiante (RF06, RNF05)",
        description = "Marca al estudiante como inactivo preservando todo el historial. " +
                      "No elimina físicamente el registro.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<Void>> darDeBaja(
            @PathVariable Long id,
            @Valid @RequestBody BajaEstudianteRequestDTO dto) {
        estudianteService.darDeBaja(id, dto);
        return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante dado de baja exitosamente."));
    }
}
