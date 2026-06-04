package com.utec.sienep.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "info_medicos", schema = "proyecto")
public class InformeMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_informe")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_info_estudiante", nullable = false)
    private Estudiante estudiante;

    @Column(name = "nom_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "tip_archivo", length = 100)
    private String tipoArchivo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fec_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_info_cargado")
    private Usuario cargadoPor;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public InformeMedico() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }
    public Usuario getCargadoPor() { return cargadoPor; }
    public void setCargadoPor(Usuario cargadoPor) { this.cargadoPor = cargadoPor; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
