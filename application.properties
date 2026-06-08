package com.utec.sienep.service;

import com.utec.sienep.entity.Auditoria;
import com.utec.sienep.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Registra un evento de auditoría.
     * Usa REQUIRES_NEW para que el log se guarde incluso si la transacción principal falla.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String username, String accion,
                          String entidad, Long entidadId,
                          String detalle, String resultado) {
        Auditoria a = new Auditoria();
        a.setUsername(username);
        a.setAccion(accion);
        a.setEntidad(entidad);
        a.setEntidadId(entidadId);
        // El detalle nunca debe contener contraseñas ni datos sensibles
        a.setDetalle(detalle);
        a.setResultado(resultado);
        a.setFechaHora(LocalDateTime.now());
        auditoriaRepository.save(a);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarExitoso(String username, String accion,
                                  String entidad, Long entidadId, String detalle) {
        registrar(username, accion, entidad, entidadId, detalle, "EXITOSO");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFallido(String username, String accion, String detalle) {
        registrar(username, accion, null, null, detalle, "FALLIDO");
    }
}
