package com.utec.sienep.repository;

import com.utec.sienep.entity.Instancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstanciaRepository extends JpaRepository<Instancia, Long> {

    List<Instancia> findByEstudianteIdAndActivoTrueOrderByFechaInstanciaDesc(Long estudianteId);

    List<Instancia> findByActivoTrueOrderByFechaInstanciaDesc();

    Optional<Instancia> findByIdentificadorAndActivoTrue(String identificador);

    Optional<Instancia> findByIdAndActivoTrue(Long id);

    boolean existsByIdentificador(String identificador);

    // Para generar el siguiente número de secuencia del día
    long countByIdentificadorStartingWith(String prefijo);
}
