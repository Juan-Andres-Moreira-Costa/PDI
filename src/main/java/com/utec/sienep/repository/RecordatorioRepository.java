package com.utec.sienep.repository;

import com.utec.sienep.entity.Recordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {

    List<Recordatorio> findByEstudianteIdAndActivoTrueOrderByFechaRecordatorioAsc(Long estudianteId);

    List<Recordatorio> findByActivoTrueOrderByFechaRecordatorioAsc();

    Optional<Recordatorio> findByIdAndActivoTrue(Long id);

    Optional<Recordatorio> findByIdentificadorAndActivoTrue(String identificador);

    // Recordatorios pendientes de notificación próximos a su fecha
    @Query("SELECT r FROM Recordatorio r WHERE r.activo = true " +
           "AND r.notificacionEnviada = false " +
           "AND r.estado = 'PENDIENTE' " +
           "AND r.fechaRecordatorio BETWEEN :ahora AND :limite")
    List<Recordatorio> findPendientesDeNotificacion(
            @Param("ahora") LocalDateTime ahora,
            @Param("limite") LocalDateTime limite);

    // Para generar identificador secuencial diario
    long countByIdentificadorStartingWith(String prefijo);

    // Recordatorios recurrentes activos
    List<Recordatorio> findByEsRecurrenteTrueAndActivoTrueAndEstado(String estado);

    // Hijos de un recordatorio recurrente padre
    List<Recordatorio> findByRecordatorioPadreId(Long padreId);
}
