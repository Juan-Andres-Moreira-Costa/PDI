package com.utec.sienep.dto.request;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class ReporteRequestDTO {

    // Filtros opcionales — todos son opcionales entre sí
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFin;

    private String carrera;
    private String grupo;
    private Long estudianteId;

    // Formato de exportación: JSON, CSV, EXCEL
    private String formato = "JSON";

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }
}
