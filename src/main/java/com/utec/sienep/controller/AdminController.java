package com.utec.sienep.controller;

import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.entity.Rol;
import com.utec.sienep.entity.Usuario;
import com.utec.sienep.service.AdminService;
import com.utec.sienep.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administración", description = "Gestión de roles, permisos y usuarios (RF32-RF34)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    // RF32-RF33 — Listar roles
    @GetMapping("/roles")
    @Operation(summary = "Listar todos los roles (RF32-RF33)")
    public ResponseEntity<ApiResponseDTO<List<Rol>>> listarRoles() {
        return ResponseEntity.ok(ApiResponseDTO.ok("Roles del sistema.", adminService.listarRoles()));
    }

    // RF34 — Crear rol personalizado
    @PostMapping("/roles")
    @Operation(summary = "Crear rol personalizado (RF34)")
    public ResponseEntity<ApiResponseDTO<Rol>> crearRol(
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Rol creado.", adminService.crearRol(nombre, descripcion)));
    }

    // Listar usuarios
    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios del sistema",
        description = "La respuesta nunca incluye contraseñas ni hashes.")
    public ResponseEntity<ApiResponseDTO<List<Usuario>>> listarUsuarios() {
        return ResponseEntity.ok(ApiResponseDTO.ok("Usuarios del sistema.", adminService.listarUsuarios()));
    }

    // Asignar rol a usuario
    @PostMapping("/usuarios/{usuarioId}/roles/{rolId}")
    @Operation(summary = "Asignar rol a usuario")
    public ResponseEntity<ApiResponseDTO<Void>> asignarRol(
            @PathVariable Long usuarioId,
            @PathVariable Long rolId) {
        adminService.asignarRol(usuarioId, rolId);
        return ResponseEntity.ok(ApiResponseDTO.ok("Rol asignado correctamente."));
    }

    // Quitar rol a usuario
    @DeleteMapping("/usuarios/{usuarioId}/roles/{rolId}")
    @Operation(summary = "Quitar rol a usuario")
    public ResponseEntity<ApiResponseDTO<Void>> quitarRol(
            @PathVariable Long usuarioId,
            @PathVariable Long rolId) {
        adminService.quitarRol(usuarioId, rolId);
        return ResponseEntity.ok(ApiResponseDTO.ok("Rol quitado correctamente."));
    }

    // RF02 — Desbloquear usuario bloqueado por intentos fallidos
    @PutMapping("/usuarios/{usuarioId}/desbloquear")
    @Operation(summary = "Desbloquear usuario (RF02)",
        description = "Desbloquea una cuenta que fue bloqueada por exceder el máximo de intentos fallidos.")
    public ResponseEntity<ApiResponseDTO<Void>> desbloquear(
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal UserDetails admin) {
        authService.desbloquearUsuario(usuarioId, admin.getUsername());
        return ResponseEntity.ok(ApiResponseDTO.ok("Usuario desbloqueado correctamente."));
    }
}
