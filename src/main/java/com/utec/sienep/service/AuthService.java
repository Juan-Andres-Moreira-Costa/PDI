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

    //Autenticación con Credenciales

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {

        // 1. Verificar si el usuario existe y si está bloqueado ANTES de intentar autenticar
        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(dto.getUsername()).orElseThrow(() -> new ReglaNegocioException("Credenciales inválidas."));

        if (usuario.isBloqueado()) {
            auditoriaService.registrarFallido(
                    dto.getUsername(), "LOGIN", "Cuenta bloqueada — acceso denegado");
            throw new ReglaNegocioException("La cuenta está bloqueada por exceso de intentos fallidos. " + "Contacte al administrador.");
        }

        // 2. Intentar autenticar con Spring Security
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

        } catch (BadCredentialsException e) {
            // 3. Incrementar contador de intentos fallidos
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);

            // 4. Bloquear si llega a 5 intentos
            if (usuario.getIntentosFallidos() >= 5) {
                usuario.setBloqueado(true);
                usuarioRepository.save(usuario);
                auditoriaService.registrarFallido(dto.getUsername(), "LOGIN", "Cuenta bloqueada tras 5 intentos fallidos");
                throw new ReglaNegocioException("Cuenta bloqueada por exceso de intentos fallidos. " + "Contacte al administrador.");
            }

            usuarioRepository.save(usuario);
            auditoriaService.registrarFallido(
                    dto.getUsername(), "LOGIN", "Intento fallido " + usuario.getIntentosFallidos() + " de 5");
            throw new ReglaNegocioException("Credenciales inválidas.");
        }

        catch (org.springframework.security.authentication.DisabledException e) {
            auditoriaService.registrarFallido(dto.getUsername(), "LOGIN", "Acceso denegado — cuenta bloqueada");
            throw new ReglaNegocioException(
                    "La cuenta está bloqueada. Contacte al administrador.");
        }

        // 5. Login exitoso — resetear contador de intentos fallidos
        usuario.setIntentosFallidos(0);
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // 6. Generar token
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());
        String token = jwtUtil.generarToken(userDetails);

        auditoriaService.registrarExitoso(
                dto.getUsername(), "LOGIN", "Usuario", null, "Login exitoso");

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority()).toList();

        return new LoginResponseDTO(
                token,
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                roles
        );
    }

    @Transactional
    public void logout(String username) {
        auditoriaService.registrarExitoso(
                username, "LOGOUT", "Usuario", null, "Cierre de sesión registrado");
    }

    //Gestión de Credenciales (cambio de contraseña)

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

    // Desbloquear usuario (ADMIN)

    @Transactional
    public void desbloquearUsuario(Long usuarioId, String adminUsername) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado: " + usuarioId));
        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(0);
        usuarioRepository.save(usuario);
        auditoriaService.registrarExitoso(adminUsername, "DESBLOQUEAR_USUARIO", "Usuario", usuarioId, "Usuario desbloqueado por administrador");
    }
}
