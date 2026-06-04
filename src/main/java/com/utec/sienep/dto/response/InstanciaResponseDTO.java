package com.utec.sienep.dto.response;

import java.time.LocalDateTime;

public class InstanciaResponseDTO {

    private Long id;
    private String identificador;
    private Long estudianteId;
    private String estudianteNombre;
    private String estudianteCedula;
    private Long categoriaId;
    private String categoriaNombre;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaInstancia;
    private Integer duracionMinutos;
    private String lugar;
    private String estado;
    private String googleCalendarEventId;
    private boolean activo;
    private LocalDateTime fechaAlta;
    private LocalDateTime fechaModificacion;
    private String creadoPorUsername;
    private Long instanciaOrigenId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }

    public Long getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Long estudianteId) { this.estudianteId = estudianteId; }

    public String getEstudianteNombre() { return estudianteNombre; }
    public void setEstudianteNombre(String estudianteNombre) { this.estudianteNombre = estudianteNombre; }

    public String getEstudianteCedula() { return estudianteCedula; }
    public void setEstudianteCedula(String estudianteCedula) { this.estudianteCedula = estudianteCedula; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getGoogleCalendarEventId() { return googleCalendarEventId; }
    public void setGoogleCalendarEventId(String googleCalendarEventId) { this.googleCalendarEventId = googleCalendarEventId; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public String getCreadoPorUsername() { return creadoPorUsername; }
    public void setCreadoPorUsername(String creadoPorUsername) { this.creadoPorUsername = creadoPorUsername; }

    public Long getInstanciaOrigenId() { return instanciaOrigenId; }
    public void setInstanciaOrigenId(Long instanciaOrigenId) { this.instanciaOrigenId = instanciaOrigenId; }
}
