package com.utec.sienep.controller;

import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.entity.Rol;
import com.utec.sienep.entity.Usuario;
import com.utec.sienep.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administración", description = "Gestión de roles, permisos y usuarios (RF32-RF34)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // RF32-RF33 – Listar roles preexistentes y personalizados
    @GetMapping("/roles")
    @Operation(summary = "Listar todos los roles (RF32-RF33)",
        description = "Retorna todos los roles del sistema, incluyendo los preexistentes " +
                      "(ROLE_ADMIN, ROLE_DOCENTE, ROLE_VIEWER) y los personalizados.")
    public ResponseEntity<ApiResponseDTO<List<Rol>>> listarRoles() {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Roles del sistema.", adminService.listarRoles()));
    }

    // RF34 – Crear rol personalizado
    @PostMapping("/roles")
    @Operation(summary = "Crear rol personalizado (RF34)",
        description = "Crea un nuevo rol personalizado. " +
                      "El nombre se normaliza automáticamente con prefijo ROLE_.")
    public ResponseEntity<ApiResponseDTO<Rol>> crearRol(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion) {
        Rol creado = adminService.crearRol(nombre, descripcion);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Rol creado.", creado));
    }

    // Listar usuarios
    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios del sistema",
        description = "Solo accesible para ADMIN. " +
                      "La respuesta nunca incluye contraseñas ni hashes.")
    public ResponseEntity<ApiResponseDTO<List<Usuario>>> listarUsuarios() {
        return ResponseEntity.ok(
                ApiResponseDTO.ok("Usuarios del sistema.", adminService.listarUsuarios()));
    }

    // Asignar rol a usuario
    @PostMapping("/usuarios/{usuarioId}/roles/{rolId}")
    @Operation(summary = "Asignar rol a usuario",
        description = "Asigna un rol existente a un usuario del sistema.")
    public ResponseEntity<ApiResponseDTO<Void>> asignarRol(
            @PathVariable Long usuarioId,
            @PathVariable Long rolId) {
        adminService.asignarRol(usuarioId, rolId);
        return ResponseEntity.ok(ApiResponseDTO.ok("Rol asignado correctamente."));
    }

    // Quitar rol a usuario
    @DeleteMapping("/usuarios/{usuarioId}/roles/{rolId}")
    @Operation(summary = "Quitar rol a usuario",
        description = "Quita un rol a un usuario. " +
                      "No se puede quitar el único rol asignado.")
    public ResponseEntity<ApiResponseDTO<Void>> quitarRol(
            @PathVariable Long usuarioId,
            @PathVariable Long rolId) {
        adminService.quitarRol(usuarioId, rolId);
        return ResponseEntity.ok(ApiResponseDTO.ok("Rol quitado correctamente."));
    }
}
