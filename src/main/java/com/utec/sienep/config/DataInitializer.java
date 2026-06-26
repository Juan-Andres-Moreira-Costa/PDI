package com.utec.sienep.config;

import com.utec.sienep.entity.Rol;
import com.utec.sienep.entity.Usuario;
import com.utec.sienep.repository.RolRepository;
import com.utec.sienep.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

//Creación del usuario administrador inicial si no existe
// Credenciales: admin / Admin1234!

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    // Usuario de desarrollo. En producción se deshabilita o cambia credenciales.
    public void run(String... args) {
        if (!usuarioRepository.existsByUsername("admin")) {
            Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN").orElseGet(() -> {
                        Rol r = new Rol("ROLE_ADMIN");
                        r.setDescripcion("Administrador del sistema");
                        return rolRepository.save(r);
                    });

            Usuario admin = new Usuario();
            admin.setUsername("admin");

            // Contraseña almacenada con BCrypt
            admin.setPasswordHash(passwordEncoder.encode("Admin1234!"));
            admin.setNombre("Administrador");
            admin.setApellido("SIENEP");
            admin.setEmail("admin@sienep.utec.edu.uy");
            admin.setActivo(true);
            admin.setFechaAlta(LocalDateTime.now());
            admin.setRoles(Set.of(rolAdmin));

            usuarioRepository.save(admin);
            System.out.println(">>> Usuario admin creado. Cambiar contraseña en el primer login.");
        }
    }
}
