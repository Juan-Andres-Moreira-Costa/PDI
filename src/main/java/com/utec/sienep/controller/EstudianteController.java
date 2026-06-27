package com.utec.sienep.controller;

import com.utec.sienep.dto.request.BajaEstudianteRequestDTO;
import com.utec.sienep.dto.request.EstudianteRequestDTO;
import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.dto.response.EstudianteResponseDTO;
import com.utec.sienep.service.EstudianteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/estudiantes")
@Tag(name = "Estudiantes", description = "Gestión completa de estudiantes" +
        "los campos sensibles (informacionSalud, observacionesConfidenciales) " +
        "solo aparecen en la respuesta según el rol del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // Alta Estudiante

    @PostMapping
    @Operation(summary = "Alta de estudiante",
            description = "Registra un nuevo estudiante. " +
                    "Valida cédula uruguaya (dígito verificador) y edad mínima 18 años. " +
                    "observacionesConfidenciales solo es persistido por ROLE_DIRECCION/ADMIN.")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','PSICOPEDAGOGO')")
    public ResponseEntity<ApiResponseDTO<EstudianteResponseDTO>> crear(
            @Valid @RequestBody EstudianteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Estudiante registrado exitosamente.",
                        estudianteService.crear(dto)));
    }

    // Búsqueda / Listado

    @GetMapping
    @Operation(summary = "Listar / buscar estudiantes activos",
            description = "Retorna los estudiantes activos con paginación. " +
                    "Filtros opcionales: **nombre** (parcial), **cedula** (exacta), " +
                    "**carrera** (parcial) e **itr** (parcial). " +
                    "Parámetros de paginación: **page** (defecto 0), **size** (defecto 20), " +
                    "**sort** (defecto 'apellido'). " + "informacionSalud solo aparece para roles autorizados."
    )
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','PSICOPEDAGOGO','TUTOR','DIRECCION','VIEWER')")
    public ResponseEntity<ApiResponseDTO<Page<EstudianteResponseDTO>>> listar(
            @Parameter(description = "Búsqueda parcial por nombre o apellido")
            @RequestParam(required = false) String nombre,

            @Parameter(description = "Búsqueda exacta por cédula")
            @RequestParam(required = false) String cedula,

            @Parameter(description = "Filtro parcial por carrera")
            @RequestParam(required = false) String carrera,

            @Parameter(description = "Filtro parcial por ITR")
            @RequestParam(required = false) String itr,

            @Parameter(description = "Número de página (0-indexado)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Cantidad de resultados por página")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Campo de ordenamiento")
            @RequestParam(defaultValue = "apellido") String sort) {

        // Validar parámetros de paginación para evitar abusos
        int safeSize = Math.min(size, 100); // máximo 100 por página
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(sort).ascending());

        // Búsqueda exacta por cédula retorna singleton envuelto en Page
        if (cedula != null && !cedula.isBlank()) {
            EstudianteResponseDTO encontrado = estudianteService.buscarPorCedula(cedula);
            // Envuelvo en Page para mantener la respuesta consistente
            Page<EstudianteResponseDTO> resultado =
                    new org.springframework.data.domain.PageImpl<>(
                            java.util.List.of(encontrado),
                            pageable, 1L);
            return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante encontrado.", resultado));
        }

        Page<EstudianteResponseDTO> resultado;

        if (nombre != null && !nombre.isBlank()) {
            resultado = estudianteService.buscarPorNombre(nombre, pageable);
        } else if (carrera != null && !carrera.isBlank()) {
            resultado = estudianteService.buscarPorCarrera(carrera, pageable);
        } else if (itr != null && !itr.isBlank()) {
            resultado = estudianteService.buscarPorItr(itr, pageable);
        } else {
            resultado = estudianteService.listarActivos(pageable);
        }

        return ResponseEntity.ok(ApiResponseDTO.ok("Listado de estudiantes activos.", resultado));
    }

    // Búsqueda por ID

    @GetMapping("/{id}")
    @Operation(summary = "Buscar estudiante por ID")
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','PSICOPEDAGOGO','TUTOR','DIRECCION','VIEWER')")
    public ResponseEntity<ApiResponseDTO<EstudianteResponseDTO>> buscarPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante encontrado.",
                estudianteService.buscarPorId(id)));
    }

    // Modificación

    @PutMapping("/{id}")
    @Operation(summary = "Modificar estudiante",
            description = "Actualiza los datos del estudiante. La cédula no puede modificarse."
    )
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE','PSICOPEDAGOGO')")
    public ResponseEntity<ApiResponseDTO<EstudianteResponseDTO>> modificar(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante modificado exitosamente.",
                estudianteService.modificar(id, dto)));
    }

    // Baja Lógica

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja lógica de estudiante",
            description = "Marca al estudiante como inactivo preservando todo el historial. " +
                    "No elimina físicamente el registro."
    )
    @PreAuthorize("hasAnyRole('ADMIN','DOCENTE')")
    public ResponseEntity<ApiResponseDTO<Void>> darDeBaja(
            @PathVariable Long id,
            @Valid @RequestBody BajaEstudianteRequestDTO dto) {
        estudianteService.darDeBaja(id, dto);
        return ResponseEntity.ok(ApiResponseDTO.ok("Estudiante dado de baja exitosamente."));
    }
}
