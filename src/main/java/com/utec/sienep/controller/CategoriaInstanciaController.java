package com.utec.sienep.controller;

import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.entity.CategoriaInstancia;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.CategoriaInstanciaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias-instancias")
@Tag(name = "Categorías de Instancias", description = "Administración de categorías (RF11, RF35-RF37)")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaInstanciaController {

    private final CategoriaInstanciaRepository categoriaRepository;

    public CategoriaInstanciaController(CategoriaInstanciaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    @Operation(summary = "Listar categorías activas (RF11)")
    public ResponseEntity<ApiResponseDTO<List<CategoriaInstancia>>> listar() {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Categorías de instancias.",
                        categoriaRepository.findByActivoTrue()));
    }

    @PostMapping
    @Operation(summary = "Agregar categoría (RF35)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<CategoriaInstancia>> crear(
            @RequestParam @NotBlank String nombre,
            @RequestParam(required = false) @Size(max = 300) String descripcion) {

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException(
                    "Ya existe una categoría con el nombre: " + nombre);
        }
        CategoriaInstancia cat = new CategoriaInstancia();
        cat.setNombre(nombre.trim());
        cat.setDescripcion(descripcion);
        cat.setActivo(true);
        CategoriaInstancia guardada = categoriaRepository.save(cat);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Categoría creada.", guardada));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar categoría (RF37)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<CategoriaInstancia>> modificar(
            @PathVariable Long id,
            @RequestParam @NotBlank String nombre,
            @RequestParam(required = false) @Size(max = 300) String descripcion) {

        CategoriaInstancia cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada: " + id));
        cat.setNombre(nombre.trim());
        cat.setDescripcion(descripcion);
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Categoría modificada.", categoriaRepository.save(cat)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría — baja lógica (RF36)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> eliminar(@PathVariable Long id) {
        CategoriaInstancia cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Categoría no encontrada: " + id));
        cat.setActivo(false);
        categoriaRepository.save(cat);
        return ResponseEntity.ok(ApiResponseDTO.ok("Categoría eliminada."));
    }
}
