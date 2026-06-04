package com.utec.sienep.controller;

import com.utec.sienep.dto.request.LoginRequestDTO;
import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.dto.response.LoginResponseDTO;
import com.utec.sienep.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login, logout y gestión de credenciales (RF01-RF04)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login (RF01-RF02)",
        description = "Autentica con usuario y contraseña. Retorna un token JWT. " +
                      "La contraseña nunca se expone en la respuesta.")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(ApiResponseDTO.ok("Login exitoso.", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (RF04)",
        description = "Registra el cierre de sesión. El cliente debe eliminar el token JWT localmente.")
    public ResponseEntity<ApiResponseDTO<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDTO.ok("Sesión cerrada correctamente."));
    }

    @PutMapping("/cambiar-password")
    @Operation(summary = "Cambiar contraseña (RF03)",
        description = "Permite al usuario autenticado cambiar su propia contraseña. " +
                      "Requiere la contraseña actual para confirmar la identidad.")
    public ResponseEntity<ApiResponseDTO<Void>> cambiarPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String passwordActual,
            @RequestParam String passwordNueva) {
        authService.cambiarPassword(
                userDetails.getUsername(), passwordActual, passwordNueva);
        return ResponseEntity.ok(ApiResponseDTO.ok("Contraseña actualizada correctamente."));
    }
}
