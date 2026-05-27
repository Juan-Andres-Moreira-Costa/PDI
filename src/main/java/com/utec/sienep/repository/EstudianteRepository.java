package com.utec.sienep.repository;

import com.utec.sienep.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    // Buscar por cédula (solo activos)
    Optional<Estudiante> findByCedulaAndActivoTrue(String cedula);

    // Verificar si existe cédula (incluyendo dados de baja)
    boolean existsByCedula(String cedula);

    // Verificar si existe email (incluyendo dados de baja)
    boolean existsByEmail(String email);

    // Verificar si existe email excluyendo un id (para modificación)
    boolean existsByEmailAndIdNot(String email, Long id);

    // Listar solo los activos
    List<Estudiante> findByActivoTrue();

    // Buscar por nombre o apellido (activos)
    @Query("SELECT e FROM Estudiante e WHERE e.activo = true AND " +
           "(LOWER(e.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.apellido) LIKE LOWER(CONCAT('%', :termino, '%')))")
    List<Estudiante> buscarPorNombreOApellido(@Param("termino") String termino);

    // Buscar por carrera (activos)
    List<Estudiante> findByCarreraAndActivoTrue(String carrera);

    // Buscar por ITR (activos)
    List<Estudiante> findByItrAndActivoTrue(String itr);

    // Buscar por id solo si está activo
    Optional<Estudiante> findByIdAndActivoTrue(Long id);
}
