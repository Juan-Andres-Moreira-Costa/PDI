package com.utec.sienep.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReporteResponseDTO {

    private String titulo;
    private LocalDateTime fechaGeneracion;
    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private String filtroCarrera;
    private String filtroGrupo;
    private Long filtroEstudianteId;

    // Métricas globales
    private int totalEstudiantesActivos;
    private int totalInstancias;
    private int totalRecordatorios;
    private int totalIncidencias;

    // Métricas de instancias
    private Map<String, Long> instanciasPorCategoria;
    private Map<String, Long> instanciasPorEstado;
    private int instanciasUltimos30Dias;

    // Métricas de recordatorios
    private int recordatoriosPendientes;
    private int recordatoriosCompletados;
    private int recordatoriosCancelados;
    private int recordatoriosRecurrentes;

    // Métricas de incidencias
    private Map<String, Long> incidenciasPorSeveridad;
    private Map<String, Long> incidenciasPorEstado;
    private int incidenciasAbiertas;

    // Detalle por estudiante (cuando se filtra por estudiante)
    private List<Map<String, Object>> detalleEstudiante;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(LocalDate periodoInicio) { this.periodoInicio = periodoInicio; }

    public LocalDate getPeriodoFin() { return periodoFin; }
    public void setPeriodoFin(LocalDate periodoFin) { this.periodoFin = periodoFin; }

    public String getFiltroCarrera() { return filtroCarrera; }
    public void setFiltroCarrera(String filtroCarrera) { this.filtroCarrera = filtroCarrera; }

    public String getFiltroGrupo() { return filtroGrupo; }
    public void setFiltroGrupo(String filtroGrupo) { this.filtroGrupo = filtroGrupo; }

    public Long getFiltroEstudianteId() { return filtroEstudianteId; }
    public void setFiltroEstudianteId(Long filtroEstudianteId) { this.filtroEstudianteId = filtroEstudianteId; }

    public int getTotalEstudiantesActivos() { return totalEstudiantesActivos; }
    public void setTotalEstudiantesActivos(int totalEstudiantesActivos) { this.totalEstudiantesActivos = totalEstudiantesActivos; }

    public int getTotalInstancias() { return totalInstancias; }
    public void setTotalInstancias(int totalInstancias) { this.totalInstancias = totalInstancias; }

    public int getTotalRecordatorios() { return totalRecordatorios; }
    public void setTotalRecordatorios(int totalRecordatorios) { this.totalRecordatorios = totalRecordatorios; }

    public int getTotalIncidencias() { return totalIncidencias; }
    public void setTotalIncidencias(int totalIncidencias) { this.totalIncidencias = totalIncidencias; }

    public Map<String, Long> getInstanciasPorCategoria() { return instanciasPorCategoria; }
    public void setInstanciasPorCategoria(Map<String, Long> instanciasPorCategoria) { this.instanciasPorCategoria = instanciasPorCategoria; }

    public Map<String, Long> getInstanciasPorEstado() { return instanciasPorEstado; }
    public void setInstanciasPorEstado(Map<String, Long> instanciasPorEstado) { this.instanciasPorEstado = instanciasPorEstado; }

    public int getInstanciasUltimos30Dias() { return instanciasUltimos30Dias; }
    public void setInstanciasUltimos30Dias(int instanciasUltimos30Dias) { this.instanciasUltimos30Dias = instanciasUltimos30Dias; }

    public int getRecordatoriosPendientes() { return recordatoriosPendientes; }
    public void setRecordatoriosPendientes(int recordatoriosPendientes) { this.recordatoriosPendientes = recordatoriosPendientes; }

    public int getRecordatoriosCompletados() { return recordatoriosCompletados; }
    public void setRecordatoriosCompletados(int recordatoriosCompletados) { this.recordatoriosCompletados = recordatoriosCompletados; }

    public int getRecordatoriosCancelados() { return recordatoriosCancelados; }
    public void setRecordatoriosCancelados(int recordatoriosCancelados) { this.recordatoriosCancelados = recordatoriosCancelados; }

    public int getRecordatoriosRecurrentes() { return recordatoriosRecurrentes; }
    public void setRecordatoriosRecurrentes(int recordatoriosRecurrentes) { this.recordatoriosRecurrentes = recordatoriosRecurrentes; }

    public Map<String, Long> getIncidenciasPorSeveridad() { return incidenciasPorSeveridad; }
    public void setIncidenciasPorSeveridad(Map<String, Long> incidenciasPorSeveridad) { this.incidenciasPorSeveridad = incidenciasPorSeveridad; }

    public Map<String, Long> getIncidenciasPorEstado() { return incidenciasPorEstado; }
    public void setIncidenciasPorEstado(Map<String, Long> incidenciasPorEstado) { this.incidenciasPorEstado = incidenciasPorEstado; }

    public int getIncidenciasAbiertas() { return incidenciasAbiertas; }
    public void setIncidenciasAbiertas(int incidenciasAbiertas) { this.incidenciasAbiertas = incidenciasAbiertas; }

    public List<Map<String, Object>> getDetalleEstudiante() { return detalleEstudiante; }
    public void setDetalleEstudiante(List<Map<String, Object>> detalleEstudiante) { this.detalleEstudiante = detalleEstudiante; }
}
