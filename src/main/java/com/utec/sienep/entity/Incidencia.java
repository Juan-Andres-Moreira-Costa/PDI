package com.utec.sienep.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias", schema = "proyecto")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_incidencia")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_inci_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_inci_instancia")
    private Instancia instancia;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "lugar", length = 200)
    private String lugar;

    @Column(name = "per_involucrada", columnDefinition = "TEXT")
    private String personasInvolucradas;

    @Column(name = "tipo", length = 100)
    private String tipo;

    @Column(name = "severidad", nullable = false, length = 50)
    private String severidad = "MEDIA";

    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "ABIERTA";

    @Column(name = "fec_incidencia", nullable = false)
    private LocalDateTime fechaIncidencia;

    @Column(name = "fec_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "resolucion", columnDefinition = "TEXT")
    private String resolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_inci_registrado")
    private Usuario registradoPor;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fec_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "fec_modificacion")
    private LocalDateTime fechaModificacion;

    public Incidencia() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Instancia getInstancia() { return instancia; }
    public void setInstancia(Instancia instancia) { this.instancia = instancia; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public String getPersonasInvolucradas() { return personasInvolucradas; }
    public void setPersonasInvolucradas(String personasInvolucradas) { this.personasInvolucradas = personasInvolucradas; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getSeveridad() { return severidad; }
    public void setSeveridad(String severidad) { this.severidad = severidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaIncidencia() { return fechaIncidencia; }
    public void setFechaIncidencia(LocalDateTime fechaIncidencia) { this.fechaIncidencia = fechaIncidencia; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public String getResolucion() { return resolucion; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }
    public Usuario getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(Usuario registradoPor) { this.registradoPor = registradoPor; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
