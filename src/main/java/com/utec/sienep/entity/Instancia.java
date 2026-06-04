package com.utec.sienep.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "instancias", schema = "proyecto")
public class Instancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_instancia")
    private Long id;

    @Column(name = "identificador", nullable = false, unique = true, length = 20)
    private String identificador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_inst_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_inst_categoria")
    private CategoriaInstancia categoria;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "not_confidencial", columnDefinition = "TEXT")
    private String notasConfidenciales;

    @Column(name = "fec_instancia", nullable = false)
    private LocalDateTime fechaInstancia;

    @Column(name = "dur_minuto")
    private Integer duracionMinutos;

    @Column(name = "lugar", length = 200)
    private String lugar;

    @Column(name = "canal", length = 100)
    private String canal;

    @Column(name = "participante", length = 500)
    private String participantes;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "PROGRAMADA";

    @Column(name = "goo_calendar", length = 255)
    private String googleCalendarEventId;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fec_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "fec_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_inst_creado")
    private Usuario creadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_inst_origen")
    private Instancia instanciaOrigen;

    public Instancia() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public CategoriaInstancia getCategoria() { return categoria; }
    public void setCategoria(CategoriaInstancia categoria) { this.categoria = categoria; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getNotasConfidenciales() { return notasConfidenciales; }
    public void setNotasConfidenciales(String notasConfidenciales) { this.notasConfidenciales = notasConfidenciales; }
    public LocalDateTime getFechaInstancia() { return fechaInstancia; }
    public void setFechaInstancia(LocalDateTime fechaInstancia) { this.fechaInstancia = fechaInstancia; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getParticipantes() { return participantes; }
    public void setParticipantes(String participantes) { this.participantes = participantes; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getGoogleCalendarEventId() { return googleCalendarEventId; }
    public void setGoogleCalendarEventId(String googleCalendarEventId) { this.googleCalendarEventId = googleCalendarEventId; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    public Usuario getCreadoPor() { return creadoPor; }
    public void setCreadoPor(Usuario creadoPor) { this.creadoPor = creadoPor; }
    public Instancia getInstanciaOrigen() { return instanciaOrigen; }
    public void setInstanciaOrigen(Instancia instanciaOrigen) { this.instanciaOrigen = instanciaOrigen; }
}
