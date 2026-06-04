package com.utec.sienep.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.utec.sienep.entity.Auditoria;
import com.utec.sienep.repository.AuditoriaRepository;

import java.time.LocalDateTime;

/**
 * RNF12 – Notificaciones Externas.
 * Envía notificaciones por correo electrónico.
 * En desarrollo, el envío se puede deshabilitar con sienep.notificaciones.habilitadas=false
 * y el sistema registra el intento en auditoría de todas formas.
 */
@Service
public class NotificacionService {

    private final JavaMailSender mailSender;
    private final AuditoriaRepository auditoriaRepository;

    @Value("${sienep.notificaciones.habilitadas:false}")
    private boolean notificacionesHabilitadas;

    @Value("${sienep.notificaciones.email-remitente:sienep@utec.edu.uy}")
    private String emailRemitente;

    public NotificacionService(JavaMailSender mailSender,
                               AuditoriaRepository auditoriaRepository) {
        this.mailSender = mailSender;
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Envía un correo electrónico al destinatario indicado.
     * Registra el resultado (éxito/error) en auditoría.
     * Usa REQUIRES_NEW para que el registro persista aunque falle el envío.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviarEmail(String destinatario, String asunto, String cuerpo) {
        String resultado = "ENVIADO";
        String detalle = "Email enviado a " + destinatario + " — Asunto: " + asunto;

        if (!notificacionesHabilitadas) {
            resultado = "SIMULADO";
            detalle = "[SIMULACIÓN] " + detalle;
            registrarNotificacion(destinatario, asunto, resultado, detalle);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(emailRemitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
        } catch (Exception e) {
            resultado = "ERROR";
            detalle = "Error al enviar email a " + destinatario + ": " + e.getMessage();
        }

        registrarNotificacion(destinatario, asunto, resultado, detalle);
    }

    /**
     * Notificación de recordatorio a un usuario.
     * RF23 – Notificaciones de Recordatorios.
     */
    public void notificarRecordatorio(String emailDestinatario,
                                      String tituloRecordatorio,
                                      String identificador,
                                      String nombreEstudiante,
                                      LocalDateTime fecha) {
        String asunto = "Recordatorio SIENEP: " + tituloRecordatorio;
        String cuerpo = String.format("""
                Recordatorio del sistema SIENEP — Universidad Tecnológica (UTEC)
                
                Identificador: %s
                Estudiante: %s
                Fecha: %s
                Descripción: %s
                
                Este es un mensaje automático del sistema SIENEP.
                Por favor no responda a este correo.
                """,
                identificador, nombreEstudiante, fecha, tituloRecordatorio);

        enviarEmail(emailDestinatario, asunto, cuerpo);
    }

    /**
     * Notificación de ID de instancia al creador.
     * RF15 – Notificación de ID de Instancia.
     */
    public void notificarIdInstancia(String emailDestinatario,
                                     String identificador,
                                     String tituloInstancia,
                                     String nombreEstudiante) {
        String asunto = "Nueva instancia creada: " + identificador;
        String cuerpo = String.format("""
                Se ha registrado una nueva instancia en el sistema SIENEP.
                
                Identificador: %s
                Título: %s
                Estudiante: %s
                
                Puede consultar los detalles en el sistema SIENEP.
                """,
                identificador, tituloInstancia, nombreEstudiante);

        enviarEmail(emailDestinatario, asunto, cuerpo);
    }

    private void registrarNotificacion(String destinatario, String asunto,
                                       String resultado, String detalle) {
        Auditoria a = new Auditoria();
        a.setUsername("sistema");
        a.setAccion("NOTIFICACION_EMAIL");
        a.setEntidad("Notificacion");
        a.setDetalle(detalle);
        a.setResultado(resultado);
        a.setFechaHora(LocalDateTime.now());
        auditoriaRepository.save(a);
    }
}
