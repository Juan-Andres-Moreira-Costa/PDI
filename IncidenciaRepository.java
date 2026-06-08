package com.utec.sienep.dto.response;

import java.time.LocalDateTime;

public class IncidenciaResponseDTO {

    private Long id;
    private Long estudianteId;
    private String estudianteNombre;
    private String estudianteCedula;
    private Long instanciaId;
    private String instanciaIdentificador;
    private String titulo;
    private String descripcion;
    private String tipo;
    private String severidad;
    private String estado;
    private LocalDateTime fechaIncidencia;
    private LocalDateTime fechaCierre;
    private String resolucion;
    private String registradoPorUsername;
    private boolean activo;
    private LocalDateTime fechaAlta;
    private LocalDateTime fechaModificacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public String getEstudianteNombre() { return estudianteNombre; }
    public void setEstudianteNombre(String estudianteNombre) { this.estudianteNombre = estudianteNombre; }

    public String getEstudianteCedula() { return estudianteCedula; }
    public void setEstudianteCedula(String estudianteCedula) { this.estudianteCedula = estudianteCedula; }

    public Long getInstanciaId() { return instanciaId; }
    public void setInstanciaId(Long instanciaId) { this.instanciaId = instanciaId; }

    public String getInstanciaIdentificador() { return instanciaIdentificador; }
    public void setInstanciaIdentificador(String instanciaIdentificador) { this.instanciaIdentificador = instanciaIdentificador; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

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

    public String getRegistradoPorUsername() { return registradoPorUsername; }
    public void setRegistradoPorUsername(String registradoPorUsername) { this.registradoPorUsername = registradoPorUsername; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}
