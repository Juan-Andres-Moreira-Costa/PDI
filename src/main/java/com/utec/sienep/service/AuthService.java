package com.utec.sienep.service;

import com.utec.sienep.dto.request.LoginRequestDTO;
import com.utec.sienep.dto.response.LoginResponseDTO;
import com.utec.sienep.entity.Usuario;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.UsuarioRepository;
import com.utec.sienep.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       JwtUtil jwtUtil,
                       UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       AuditoriaService auditoriaService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    // ===================== RF02 – Autenticación con Credenciales =====================

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {
        try {
            // Spring Security valida username + password contra la BD
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    dto.getUsername(), dto.getPassword()));

        } catch (BadCredentialsException e) {
            // Auditar intento fallido sin exponer detalles del error
            auditoriaService.registrarFallido(
                dto.getUsername(), "LOGIN", "Intento de login fallido");
            throw new ReglaNegocioException("Credenciales inválidas.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());
        String token = jwtUtil.generarToken(userDetails);

        // Actualizar último login
        usuarioRepository.findByUsernameAndActivoTrue(dto.getUsername()).ifPresent(u -> {
            u.setUltimoLogin(LocalDateTime.now());
            usuarioRepository.save(u);
        });

        // Auditar login exitoso
        auditoriaService.registrarExitoso(
            dto.getUsername(), "LOGIN", "Usuario", null, "Login exitoso");

        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(dto.getUsername())
                .orElseThrow();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority()).toList();

        // La respuesta NUNCA incluye la contraseña
        return new LoginResponseDTO(
            token,
            usuario.getUsername(),
            usuario.getNombre(),
            usuario.getApellido(),
            roles
        );
    }

    // ===================== RF04 – Cierre de Sesión =====================
    // JWT es stateless: el cierre de sesión se maneja en el cliente eliminando el token.
    // En el servidor registramos el evento de auditoría.

    @Transactional
    public void logout(String username) {
        auditoriaService.registrarExitoso(
            username, "LOGOUT", "Usuario", null, "Cierre de sesión registrado");
    }

    // ===================== RF03 – Gestión de Credenciales (cambio de contraseña) =====================

    @Transactional
    public void cambiarPassword(String username, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado."));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            auditoriaService.registrarFallido(username, "CAMBIO_PASSWORD",
                "Contraseña actual incorrecta");
            throw new ReglaNegocioException("La contraseña actual es incorrecta.");
        }

        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(passwordNueva, usuario.getPasswordHash())) {
            throw new ReglaNegocioException(
                "La nueva contraseña debe ser diferente a la actual.");
        }

        // Almacenar siempre hasheada con BCrypt — nunca en texto plano
        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);

        auditoriaService.registrarExitoso(username, "CAMBIO_PASSWORD",
            "Usuario", usuario.getId(), "Contraseña actualizada correctamente");
    }
}
