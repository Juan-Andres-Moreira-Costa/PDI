package com.utec.sienep.service;

import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.entity.Rol;
import com.utec.sienep.entity.Usuario;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.RolRepository;
import com.utec.sienep.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public AdminService(RolRepository rolRepository,
                        UsuarioRepository usuarioRepository,
                        AuditoriaService auditoriaService) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    // ===================== RF32-RF33 – Gestión y roles preexistentes =====================

    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    // RF34 – Roles personalizados: crear un nuevo rol
    @Transactional
    public Rol crearRol(String nombre, String descripcion) {
        if (!nombre.startsWith("ROLE_")) {
            nombre = "ROLE_" + nombre.toUpperCase();
        }
        final String nombreFinal = nombre;

        if (rolRepository.findByNombre(nombreFinal).isPresent()) {
            throw new ReglaNegocioException("Ya existe un rol con el nombre: " + nombreFinal);
        }

        Rol rol = new Rol();
        rol.setNombre(nombreFinal);
        rol.setDescripcion(descripcion);
        Rol guardado = rolRepository.save(rol);

        auditoriaService.registrarExitoso(getUsername(), "ALTA_ROL",
                "Rol", guardado.getId(), "Rol creado: " + nombreFinal);
        return guardado;
    }

    // ===================== Asignar rol a usuario =====================

    @Transactional
    public void asignarRol(Long usuarioId, Long rolId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado: " + usuarioId));
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Rol no encontrado: " + rolId));

        if (usuario.getRoles().contains(rol)) {
            throw new ReglaNegocioException("El usuario ya tiene asignado el rol: " + rol.getNombre());
        }

        usuario.getRoles().add(rol);
        usuarioRepository.save(usuario);

        auditoriaService.registrarExitoso(getUsername(), "ASIGNAR_ROL",
                "Usuario", usuarioId,
                "Rol " + rol.getNombre() + " asignado a usuario ID " + usuarioId);
    }

    // ===================== Quitar rol de usuario =====================

    @Transactional
    public void quitarRol(Long usuarioId, Long rolId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Usuario no encontrado: " + usuarioId));
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Rol no encontrado: " + rolId));

        if (!usuario.getRoles().contains(rol)) {
            throw new ReglaNegocioException(
                    "El usuario no tiene asignado el rol: " + rol.getNombre());
        }
        if (usuario.getRoles().size() == 1) {
            throw new ReglaNegocioException(
                    "No se puede quitar el único rol del usuario. Asigná otro primero.");
        }

        usuario.getRoles().remove(rol);
        usuarioRepository.save(usuario);

        auditoriaService.registrarExitoso(getUsername(), "QUITAR_ROL",
                "Usuario", usuarioId,
                "Rol " + rol.getNombre() + " quitado a usuario ID " + usuarioId);
    }

    // ===================== Listar usuarios =====================

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    private String getUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "sistema";
        }
    }
}
