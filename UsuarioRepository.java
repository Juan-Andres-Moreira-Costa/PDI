package com.utec.sienep.dto.request;

import jakarta.validation.constraints.*;

public class IncidenciaRequestDTO {

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    private Long instanciaId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres")
    private String descripcion;

    @Size(max = 100, message = "El tipo no puede superar los 100 caracteres")
    private String tipo;

    // BAJA, MEDIA, ALTA, CRITICA
    private String severidad = "MEDIA";

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public Long getInstanciaId() { return instanciaId; }
    public void setInstanciaId(Long instanciaId) { this.instanciaId = instanciaId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getSeveridad() { return severidad; }
    public void setSeveridad(String severidad) { this.severidad = severidad; }
}
