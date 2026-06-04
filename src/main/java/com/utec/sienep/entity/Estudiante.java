package com.utec.sienep.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "estudiantes", schema = "proyecto")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_estudiante")
    private Long id;

    @Column(name = "cedula", nullable = false, unique = true, length = 8)
    private String cedula;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "fec_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 250)
    private String direccion;

    @Column(name = "itr", length = 100)
    private String itr;

    @Column(name = "carrera", length = 150)
    private String carrera;

    @Column(name = "grupo", length = 50)
    private String grupo;

    @Column(name = "sis_salud", length = 150)
    private String sistemaSalud;

    @Column(name = "mot_derivacion", columnDefinition = "TEXT")
    private String motivoDerivacion;

    @Column(name = "inf_salud", columnDefinition = "TEXT")
    private String informacionSalud;

    @Column(name = "obs_confidencial", columnDefinition = "TEXT")
    private String observacionesConfidenciales;

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fec_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "fec_baja")
    private LocalDateTime fechaBaja;

    @Column(name = "fec_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "mot_baja", length = 500)
    private String motivoBaja;

    public Estudiante() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getItr() { return itr; }
    public void setItr(String itr) { this.itr = itr; }
    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    public String getSistemaSalud() { return sistemaSalud; }
    public void setSistemaSalud(String sistemaSalud) { this.sistemaSalud = sistemaSalud; }
    public String getMotivoDerivacion() { return motivoDerivacion; }
    public void setMotivoDerivacion(String motivoDerivacion) { this.motivoDerivacion = motivoDerivacion; }
    public String getInformacionSalud() { return informacionSalud; }
    public void setInformacionSalud(String informacionSalud) { this.informacionSalud = informacionSalud; }
    public String getObservacionesConfidenciales() { return observacionesConfidenciales; }
    public void setObservacionesConfidenciales(String v) { this.observacionesConfidenciales = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }
    public LocalDateTime getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDateTime fechaBaja) { this.fechaBaja = fechaBaja; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime v) { this.fechaModificacion = v; }
    public String getMotivoBaja() { return motivoBaja; }
    public void setMotivoBaja(String motivoBaja) { this.motivoBaja = motivoBaja; }
}
