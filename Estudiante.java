package com.utec.sienep.repository;

import com.utec.sienep.entity.InformeMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InformeMedicoRepository extends JpaRepository<InformeMedico, Long> {
    List<InformeMedico> findByEstudianteIdAndActivoTrue(Long estudianteId);
}
