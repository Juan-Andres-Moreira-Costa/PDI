package com.utec.sienep.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class RecordatorioRequestDTO {

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    private Long categoriaId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha del recordatorio es obligatoria")
    private LocalDateTime fechaRecordatorio;

    // RF20 – Recurrencia
    private boolean esRecurrente = false;

    // DIARIA, SEMANAL, QUINCENAL, MENSUAL
    private String frecuenciaRecurrencia;

    private LocalDateTime fechaFinRecurrencia;

    // RF21 – Tipo: GENERAL, ACADEMICO, MEDICO, ADMINISTRATIVO
    private String tipo = "GENERAL";

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

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
}
