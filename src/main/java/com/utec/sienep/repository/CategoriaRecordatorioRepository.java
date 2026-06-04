package com.utec.sienep.repository;

import com.utec.sienep.entity.CategoriaRecordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRecordatorioRepository extends JpaRepository<CategoriaRecordatorio, Long> {
    List<CategoriaRecordatorio> findByActivoTrue();
    boolean existsByNombreIgnoreCase(String nombre);
}
