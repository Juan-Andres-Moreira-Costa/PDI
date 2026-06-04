package com.utec.sienep.controller;

import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.entity.CategoriaRecordatorio;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.CategoriaRecordatorioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias-recordatorios")
@Tag(name = "Categorías de Recordatorios",
     description = "Administración de categorías de recordatorios (RF38-RF40)")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaRecordatorioController {

    private final CategoriaRecordatorioRepository categoriaRepository;

    public CategoriaRecordatorioController(
            CategoriaRecordatorioRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    @Operation(summary = "Listar categorías de recordatorios activas")
    public ResponseEntity<ApiResponseDTO<List<CategoriaRecordatorio>>> listar() {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Categorías de recordatorios.",
                        categoriaRepository.findByActivoTrue()));
    }

    @PostMapping
    @Operation(summary = "Agregar categoría de recordatorio (RF38)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<CategoriaRecordatorio>> crear(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion) {
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException(
                    "Ya existe una categoría con el nombre: " + nombre);
        }
        CategoriaRecordatorio cat = new CategoriaRecordatorio();
        cat.setNombre(nombre.trim());
        cat.setDescripcion(descripcion);
        cat.setActivo(true);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Categoría creada.",
                        categoriaRepository.save(cat)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar categoría de recordatorio (RF40)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<CategoriaRecordatorio>> modificar(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion) {
        CategoriaRecordatorio cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada: " + id));
        cat.setNombre(nombre.trim());
        cat.setDescripcion(descripcion);
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Categoría modificada.",
                        categoriaRepository.save(cat)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría de recordatorio — baja lógica (RF39)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> eliminar(@PathVariable Long id) {
        CategoriaRecordatorio cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada: " + id));
        cat.setActivo(false);
        categoriaRepository.save(cat);
        return ResponseEntity.ok(ApiResponseDTO.ok("Categoría eliminada."));
    }
}
