package com.utec.sienep.service;

import com.utec.sienep.dto.request.ChangePasswordRequestDTO;
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

    private static final int MAX_INTENTOS = 5;

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

        // Verificar si el usuario existe y está bloqueado ANTES de intentar autenticar
        usuarioRepository.findByUsernameAndActivoTrue(dto.getUsername()).ifPresent(u -> {
            if (u.isBloqueado()) {
                auditoriaService.registrarFallido(dto.getUsername(), "LOGIN",
                        "Intento de login en cuenta bloqueada");
                throw new ReglaNegocioException(
                        "La cuenta está bloqueada por demasiados intentos fallidos. " +
                        "Contacte al administrador.");
            }
        });

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    dto.getUsername(), dto.getPassword()));

        } catch (BadCredentialsException e) {
            // RF02 – Registrar intento fallido e incrementar contador
            usuarioRepository.findByUsernameAndActivoTrue(dto.getUsername()).ifPresent(u -> {
                int intentos = u.getIntentosFallidos() + 1;
                u.setIntentosFallidos(intentos);
                if (intentos >= MAX_INTENTOS) {
                    u.setBloqueado(true);
                }
                usuarioRepository.save(u);
            });
            auditoriaService.registrarFallido(dto.getUsername(), "LOGIN",
                    "Credenciales inválidas");
            throw new ReglaNegocioException("Credenciales inválidas.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());
        String token = jwtUtil.generarToken(userDetails);

        // Resetear intentos fallidos y actualizar último login
        usuarioRepository.findByUsernameAndActivoTrue(dto.getUsername()).ifPresent(u -> {
            u.setIntentosFallidos(0);
            u.setBloqueado(false);
            u.setUltimoLogin(LocalDateTime.now());
            usuarioRepository.save(u);
        });

        auditoriaService.registrarExitoso(dto.getUsername(), "LOGIN",
                "Usuario", null, "Login exitoso");

        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(dto.getUsername())
                .orElseThrow();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority()).toList();

        return new LoginResponseDTO(token, usuario.getUsername(),
                usuario.getNombre(), usuario.getApellido(), roles);
    }

    // ===================== RF04 – Cierre de Sesión =====================

    @Transactional
    public void logout(String username) {
        auditoriaService.registrarExitoso(username, "LOGOUT",
                "Usuario", null, "Cierre de sesión registrado");
    }

    // ===================== RF03 – Gestión de Credenciales =====================

    @Transactional
    public void cambiarPassword(String username, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado."));

        if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            auditoriaService.registrarFallido(username, "CAMBIO_PASSWORD",
                    "Contraseña actual incorrecta");
            throw new ReglaNegocioException("La contraseña actual es incorrecta.");
        }
        if (passwordEncoder.matches(passwordNueva, usuario.getPasswordHash())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser diferente a la actual.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);

        auditoriaService.registrarExitoso(username, "CAMBIO_PASSWORD",
                "Usuario", usuario.getId(), "Contraseña actualizada correctamente");
    }

    // ===================== Desbloquear usuario (ADMIN) =====================

    @Transactional
    public void desbloquearUsuario(Long usuarioId, String adminUsername) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado: " + usuarioId));
        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);
        auditoriaService.registrarExitoso(adminUsername, "DESBLOQUEAR_USUARIO",
                "Usuario", usuarioId, "Usuario desbloqueado por administrador");
    }
}
