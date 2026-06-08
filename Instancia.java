package com.utec.sienep.repository;

import com.utec.sienep.entity.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    // RF29 – Historial por estudiante (todas, incluyendo cerradas)
    List<Incidencia> findByEstudianteIdOrderByFechaIncidenciaDesc(Long estudianteId);

    // Solo activas
    List<Incidencia> findByEstudianteIdAndActivoTrueOrderByFechaIncidenciaDesc(Long estudianteId);

    List<Incidencia> findByActivoTrueOrderByFechaIncidenciaDesc();

    Optional<Incidencia> findByIdAndActivoTrue(Long id);

    List<Incidencia> findByEstudianteIdAndEstadoOrderByFechaIncidenciaDesc(
            Long estudianteId, String estado);
}
