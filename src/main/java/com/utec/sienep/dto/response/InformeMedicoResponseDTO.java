package com.utec.sienep.dto.response;

import java.time.LocalDateTime;

public class InformeMedicoResponseDTO {

    private Long id;
    private Long estudianteId;
    private String estudianteNombre;
    private String nombreArchivo;
    private String tipoArchivo;
    private String descripcion;
    private LocalDateTime fechaCarga;
    private String cargadoPorUsername;
    private boolean activo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public String getEstudianteNombre() { return estudianteNombre; }
    public void setEstudianteNombre(String estudianteNombre) { this.estudianteNombre = estudianteNombre; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }

    public String getCargadoPorUsername() { return cargadoPorUsername; }
    public void setCargadoPorUsername(String cargadoPorUsername) { this.cargadoPorUsername = cargadoPorUsername; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
