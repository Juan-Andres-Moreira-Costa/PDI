package com.utec.sienep.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recordatorios", schema = "proyecto")
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_recordatorio")
    private Long id;

    @Column(name = "identificador", nullable = false, unique = true, length = 20)
    private String identificador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_reco_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_reco_categoria")
    private CategoriaRecordatorio categoria;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fec_recordatorio", nullable = false)
    private LocalDateTime fechaRecordatorio;

    @Column(name = "es_recurrente", nullable = false)
    private boolean esRecurrente = false;

    @Column(name = "fre_recurrencia", length = 20)
    private String frecuenciaRecurrencia;

    @Column(name = "fec_fin")
    private LocalDateTime fechaFinRecurrencia;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo = "GENERAL";

    @Column(name = "goo_calendar", length = 255)
    private String googleCalendarEventId;

    @Column(name = "not_enviada", nullable = false)
    private boolean notificacionEnviada = false;

    @Column(name = "fec_notificacion")
    private LocalDateTime fechaNotificacion;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "PENDIENTE";

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fec_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "fec_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_reco_creado")
    private Usuario creadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_reco_instancia")
    private Instancia instanciaGenerada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_reco_padre")
    private Recordatorio recordatorioPadre;

    public Recordatorio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public CategoriaRecordatorio getCategoria() { return categoria; }
    public void setCategoria(CategoriaRecordatorio categoria) { this.categoria = categoria; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaRecordatorio() { return fechaRecordatorio; }
    public void setFechaRecordatorio(LocalDateTime fechaRecordatorio) { this.fechaRecordatorio = fechaRecordatorio; }
    public boolean isEsRecurrente() { return esRecurrente; }
    public void setEsRecurrente(boolean esRecurrente) { this.esRecurrente = esRecurrente; }
    public String getFrecuenciaRecurrencia() { return frecuenciaRecurrencia; }
    public void setFrecuenciaRecurrencia(String frecuenciaRecurrencia) { this.frecuenciaRecurrencia = frecuenciaRecurrencia; }
    public LocalDateTime getFechaFinRecurrencia() { return fechaFinRecurrencia; }
    public void setFechaFinRecurrencia(LocalDateTime fechaFinRecurrencia) { this.fechaFinRecurrencia = fechaFinRecurrencia; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getGoogleCalendarEventId() { return googleCalendarEventId; }
    public void setGoogleCalendarEventId(String googleCalendarEventId) { this.googleCalendarEventId = googleCalendarEventId; }
    public boolean isNotificacionEnviada() { return notificacionEnviada; }
    public void setNotificacionEnviada(boolean notificacionEnviada) { this.notificacionEnviada = notificacionEnviada; }
    public LocalDateTime getFechaNotificacion() { return fechaNotificacion; }
    public void setFechaNotificacion(LocalDateTime fechaNotificacion) { this.fechaNotificacion = fechaNotificacion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    public Usuario getCreadoPor() { return creadoPor; }
    public void setCreadoPor(Usuario creadoPor) { this.creadoPor = creadoPor; }
    public Instancia getInstanciaGenerada() { return instanciaGenerada; }
    public void setInstanciaGenerada(Instancia instanciaGenerada) { this.instanciaGenerada = instanciaGenerada; }
    public Recordatorio getRecordatorioPadre() { return recordatorioPadre; }
    public void setRecordatorioPadre(Recordatorio recordatorioPadre) { this.recordatorioPadre = recordatorioPadre; }
}
