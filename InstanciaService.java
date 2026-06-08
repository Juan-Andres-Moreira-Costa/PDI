package com.utec.sienep.repository;

import com.utec.sienep.entity.CategoriaInstancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaInstanciaRepository extends JpaRepository<CategoriaInstancia, Long> {
    List<CategoriaInstancia> findByActivoTrue();
    boolean existsByNombreIgnoreCase(String nombre);
}
