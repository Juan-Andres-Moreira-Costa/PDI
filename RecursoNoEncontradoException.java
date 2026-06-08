package com.utec.sienep.dto.response;

import java.time.LocalDateTime;

public class RecordatorioResponseDTO {

    private Long id;
    private String identificador;
    private Long estudianteId;
    private String estudianteNombre;
    private String estudianteCedula;
    private Long categoriaId;
    private String categoriaNombre;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaRecordatorio;
    private boolean esRecurrente;
    private String frecuenciaRecurrencia;
    private LocalDateTime fechaFinRecurrencia;
    private String tipo;
    private String googleCalendarEventId;
    private boolean notificacionEnviada;
    private LocalDateTime fechaNotificacion;
    private String estado;
    private boolean activo;
    private LocalDateTime fechaAlta;
    private LocalDateTime fechaModificacion;
    private String creadoPorUsername;
    private Long instanciaGeneradaId;
    private String instanciaGeneradaIdentificador;
    private Long recordatorioPadreId;

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

    public String getGoogleCalendarEventId() { return googleCalendarEventId; }
    public void setGoogleCalendarEventId(String googleCalendarEventId) { this.googleCalendarEventId = googleCalendarEventId; }

    public boolean isNotificacionEnviada() { return notificacionEnviada; }
    public void setNotificacionEnviada(boolean notificacionEnviada) { this.notificacionEnviada = notificacionEnviada; }

    public LocalDateTime getFechaNotificacion() { return fechaNotificacion; }
    public void setFechaNotificacion(LocalDateTime fechaNotificacion) { this.fechaNotificacion = fechaNotificacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDateTime fechaAlta) { this.fechaAlta = fechaAlta; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public String getCreadoPorUsername() { return creadoPorUsername; }
    public void setCreadoPorUsername(String creadoPorUsername) { this.creadoPorUsername = creadoPorUsername; }

    public Long getInstanciaGeneradaId() { return instanciaGeneradaId; }
    public void setInstanciaGeneradaId(Long instanciaGeneradaId) { this.instanciaGeneradaId = instanciaGeneradaId; }

    public String getInstanciaGeneradaIdentificador() { return instanciaGeneradaIdentificador; }
    public void setInstanciaGeneradaIdentificador(String instanciaGeneradaIdentificador) { this.instanciaGeneradaIdentificador = instanciaGeneradaIdentificador; }

    public Long getRecordatorioPadreId() { return recordatorioPadreId; }
    public void setRecordatorioPadreId(Long recordatorioPadreId) { this.recordatorioPadreId = recordatorioPadreId; }
}
