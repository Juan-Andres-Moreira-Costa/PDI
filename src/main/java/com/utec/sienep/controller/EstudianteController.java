package com.utec.sienep.controller;

import com.utec.sienep.dto.request.BajaEstudianteRequestDTO;
import com.utec.sienep.dto.request.EstudianteRequestDTO;
import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.dto.response.EstudianteResponseDTO;
import com.utec.sienep.service.EstudianteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estudiantes")
@Tag(name = "Estudiantes", description = "Gestión de estudiantes del sistema SIENEP (RF05-RF08)")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // ===================== RF05 – Alta =====================

    @PostMapping
    @Operation(
        summary = "Alta de estudiante (RF05)",
        description = "Registra un nuevo estudiante. Valida cédula uruguaya (dígito verificador) y edad mínima de 18 años."
    )
    public ResponseEntity<ApiResponseDTO<EstudianteResponseDTO>> crear(
            @Valid @RequestBody EstudianteRequestDTO dto) {

        EstudianteResponseDTO creado = estudianteService.crear(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Estudiante registrado exitosamente.", creado));
    }

    // ===================== RF08 – Búsqueda / Listado =====================

    @GetMapping
    @Operation(
        summary = "Listar estudiantes activos (RF08)",
        description = "Retorna todos los estudiantes activos. Acepta filtro opcional por nombre/apellido o cédula."
    )
    public ResponseEntity<ApiResponseDTO<List<EstudianteResponseDTO>>> listar(
            @Parameter(description = "Filtro por nombre o apellido (parcial)")
            @RequestParam(required = false) String nombre,
            @Parameter(description = "Filtro por cédula exacta")
            @RequestParam(required = false) String cedula) {

        if (cedula != null && !cedula.isBlank()) {
            EstudianteResponseDTO estudiante = estudianteService.buscarPorCedula(cedula);
            return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante encontrado.", List.of(estudiante)));
        }

        if (nombre != null && !nombre.isBlank()) {
            List<EstudianteResponseDTO> lista = estudianteService.buscarPorNombre(nombre);
            return ResponseEntity.ok(ApiResponseDTO.ok("Búsqueda completada.", lista));
        }

        List<EstudianteResponseDTO> lista = estudianteService.listarActivos();
        return ResponseEntity.ok(ApiResponseDTO.ok("Listado de estudiantes activos.", lista));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar estudiante por ID (RF08)",
        description = "Retorna el estudiante activo con el ID indicado."
    )
    public ResponseEntity<ApiResponseDTO<EstudianteResponseDTO>> buscarPorId(
            @PathVariable Long id) {

        EstudianteResponseDTO estudiante = estudianteService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante encontrado.", estudiante));
    }

    // ===================== RF07 – Modificación =====================

    @PutMapping("/{id}")
    @Operation(
        summary = "Modificar estudiante (RF07)",
        description = "Actualiza los datos de un estudiante activo. La cédula no puede modificarse."
    )
    public ResponseEntity<ApiResponseDTO<EstudianteResponseDTO>> modificar(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteRequestDTO dto) {

        EstudianteResponseDTO actualizado = estudianteService.modificar(id, dto);
        return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante modificado exitosamente.", actualizado));
    }

    // ===================== RF06 – Baja Lógica =====================

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Baja lógica de estudiante (RF06)",
        description = "Realiza la baja lógica del estudiante (no elimina el registro). Requiere motivo de baja."
    )
    public ResponseEntity<ApiResponseDTO<Void>> darDeBaja(
            @PathVariable Long id,
            @Valid @RequestBody BajaEstudianteRequestDTO dto) {

        estudianteService.darDeBaja(id, dto);
        return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante dado de baja exitosamente."));
    }
}
