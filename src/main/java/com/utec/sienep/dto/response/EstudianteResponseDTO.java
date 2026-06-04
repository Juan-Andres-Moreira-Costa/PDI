package com.utec.sienep.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta de Estudiante.
 * RNF01/RD01: los campos sensibles (informacionSalud, observacionesConfidenciales)
 * solo se incluyen en la respuesta cuando el usuario tiene el rol correspondiente.
 * Se usa @JsonInclude(NON_NULL) para que campos no autorizados simplemente no aparezcan.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EstudianteResponseDTO {

    private Long id;
    private String cedula;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String direccion;
    private String itr;
    private String carrera;
    private String grupo;
    private String sistemaSalud;
    private String motivoDerivacion;
    private String observaciones;

    /**
     * RNF01 — Solo se incluye para ROLE_PSICOPEDAGOGO, ROLE_ADMIN, ROLE_DIRECCION.
     * El servicio lo setea en null para otros roles → @JsonInclude lo omite.
     */
    private String informacionSalud;

    /**
     * RD01 — Solo se incluye para ROLE_DIRECCION y ROLE_ADMIN.
     * El servicio lo setea en null para todos los demás roles.
     */
    private String observacionesConfidenciales;

    private boolean activo;
    private LocalDateTime fechaAlta;
    private LocalDateTime fechaModificacion;

    // ── Getters y Setters ─────────────────────────────────────────────────────

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

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getInformacionSalud() { return informacionSalud; }
    public void setInformacionSalud(String informacionSalud) { this.informacionSalud = informacionSalud; }

    public String getObservacionesConfidenciales() { return observacionesConfidenciales; }
    public void setObservacionesConfidenciales(String observacionesConfidenciales) {
        this.observacionesConfidenciales = observacionesConfidenciales;
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
