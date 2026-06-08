package com.utec.sienep.repository;

import com.utec.sienep.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByUsernameOrderByFechaHoraDesc(String username);
    List<Auditoria> findByEntidadAndEntidadIdOrderByFechaHoraDesc(String entidad, Long entidadId);
}
