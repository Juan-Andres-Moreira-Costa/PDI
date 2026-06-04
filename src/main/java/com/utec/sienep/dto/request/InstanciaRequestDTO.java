package com.utec.sienep.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class InstanciaRequestDTO {

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    private Long categoriaId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha de la instancia es obligatoria")
    private LocalDateTime fechaInstancia;

    @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
    private Integer duracionMinutos;

    @Size(max = 200, message = "El lugar no puede superar los 200 caracteres")
    private String lugar;

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaInstancia() { return fechaInstancia; }
    public void setFechaInstancia(LocalDateTime fechaInstancia) { this.fechaInstancia = fechaInstancia; }

    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
}
