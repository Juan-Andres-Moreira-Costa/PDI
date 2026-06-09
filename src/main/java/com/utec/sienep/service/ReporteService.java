package com.utec.sienep.service;

import com.utec.sienep.dto.request.ReporteRequestDTO;
import com.utec.sienep.dto.response.ReporteResponseDTO;
import com.utec.sienep.entity.*;
import com.utec.sienep.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final EstudianteRepository estudianteRepository;
    private final InstanciaRepository instanciaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final AuditoriaService auditoriaService;

    public ReporteService(EstudianteRepository estudianteRepository,
                          InstanciaRepository instanciaRepository,
                          RecordatorioRepository recordatorioRepository,
                          IncidenciaRepository incidenciaRepository,
                          AuditoriaService auditoriaService) {
        this.estudianteRepository = estudianteRepository;
        this.instanciaRepository = instanciaRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.auditoriaService = auditoriaService;
    }

    // ===================== RF30 – Generación de Reportes =====================

    @Transactional(readOnly = true)
    public ReporteResponseDTO generar(ReporteRequestDTO req) {
        ReporteResponseDTO reporte = new ReporteResponseDTO();
        reporte.setTitulo(construirTitulo(req));
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setPeriodoInicio(req.getFechaInicio());
        reporte.setPeriodoFin(req.getFechaFin());
        reporte.setFiltroCarrera(req.getCarrera());
        reporte.setFiltroGrupo(req.getGrupo());
        reporte.setFiltroEstudianteId(req.getEstudianteId());

        // ── Métricas de estudiantes ───────────────────────────────────────
        List<Estudiante> estudiantes = obtenerEstudiantes(req);
        reporte.setTotalEstudiantesActivos(estudiantes.size());

        // ── Métricas de instancias ────────────────────────────────────────
        List<Instancia> instancias = obtenerInstancias(req, estudiantes);
        reporte.setTotalInstancias(instancias.size());

        Map<String, Long> instanciasPorCategoria = instancias.stream()
                .filter(i -> i.getCategoria() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getCategoria().getNombre(), Collectors.counting()));
        reporte.setInstanciasPorCategoria(instanciasPorCategoria);

        Map<String, Long> instanciasPorEstado = instancias.stream()
                .collect(Collectors.groupingBy(Instancia::getEstado, Collectors.counting()));
        reporte.setInstanciasPorEstado(instanciasPorEstado);

        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        long instanciasRecientes = instancias.stream()
                .filter(i -> i.getFechaAlta().isAfter(hace30Dias)).count();
        reporte.setInstanciasUltimos30Dias((int) instanciasRecientes);

        // ── Métricas de recordatorios ─────────────────────────────────────
        List<Recordatorio> recordatorios = obtenerRecordatorios(req, estudiantes);
        reporte.setTotalRecordatorios(recordatorios.size());
        reporte.setRecordatoriosPendientes(
                (int) recordatorios.stream().filter(r -> "PENDIENTE".equals(r.getEstado())).count());
        reporte.setRecordatoriosCompletados(
                (int) recordatorios.stream().filter(r -> "COMPLETADO".equals(r.getEstado())).count());
        reporte.setRecordatoriosCancelados(
                (int) recordatorios.stream().filter(r -> "CANCELADO".equals(r.getEstado())).count());
        reporte.setRecordatoriosRecurrentes(
                (int) recordatorios.stream().filter(Recordatorio::isEsRecurrente).count());

        // ── Métricas de incidencias ───────────────────────────────────────
        List<Incidencia> incidencias = obtenerIncidencias(req, estudiantes);
        reporte.setTotalIncidencias(incidencias.size());

        Map<String, Long> incPorSeveridad = incidencias.stream()
                .collect(Collectors.groupingBy(Incidencia::getSeveridad, Collectors.counting()));
        reporte.setIncidenciasPorSeveridad(incPorSeveridad);

        Map<String, Long> incPorEstado = incidencias.stream()
                .collect(Collectors.groupingBy(Incidencia::getEstado, Collectors.counting()));
        reporte.setIncidenciasPorEstado(incPorEstado);

        reporte.setIncidenciasAbiertas(
                (int) incidencias.stream().filter(i -> "ABIERTA".equals(i.getEstado())).count());

        // ── Detalle por estudiante (solo cuando se filtra por uno) ────────
        if (req.getEstudianteId() != null && !estudiantes.isEmpty()) {
            reporte.setDetalleEstudiante(construirDetalleEstudiante(
                    estudiantes.get(0), instancias, recordatorios, incidencias));
        }

        auditoriaService.registrarExitoso("sistema", "GENERACION_REPORTE",
                "Reporte", null, reporte.getTitulo());

        return reporte;
    }

    // ===================== RF31 – Exportación CSV =====================

    @Transactional(readOnly = true)
    public byte[] exportarCsv(ReporteRequestDTO req) {
        ReporteResponseDTO reporte = generar(req);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        pw.println("REPORTE SIENEP - " + reporte.getTitulo());
        pw.println("Fecha de generacion," + reporte.getFechaGeneracion());
        pw.println("");

        pw.println("=== METRICAS GENERALES ===");
        pw.println("Estudiantes activos," + reporte.getTotalEstudiantesActivos());
        pw.println("Total instancias," + reporte.getTotalInstancias());
        pw.println("Total recordatorios," + reporte.getTotalRecordatorios());
        pw.println("Total incidencias," + reporte.getTotalIncidencias());
        pw.println("");

        pw.println("=== INSTANCIAS POR CATEGORIA ===");
        pw.println("Categoria,Cantidad");
        if (reporte.getInstanciasPorCategoria() != null) {
            reporte.getInstanciasPorCategoria()
                    .forEach((k, v) -> pw.println(k + "," + v));
        }
        pw.println("");

        pw.println("=== INSTANCIAS POR ESTADO ===");
        pw.println("Estado,Cantidad");
        if (reporte.getInstanciasPorEstado() != null) {
            reporte.getInstanciasPorEstado()
                    .forEach((k, v) -> pw.println(k + "," + v));
        }
        pw.println("");

        pw.println("=== RECORDATORIOS ===");
        pw.println("Pendientes," + reporte.getRecordatoriosPendientes());
        pw.println("Completados," + reporte.getRecordatoriosCompletados());
        pw.println("Cancelados," + reporte.getRecordatoriosCancelados());
        pw.println("Recurrentes," + reporte.getRecordatoriosRecurrentes());
        pw.println("");

        pw.println("=== INCIDENCIAS POR SEVERIDAD ===");
        pw.println("Severidad,Cantidad");
        if (reporte.getIncidenciasPorSeveridad() != null) {
            reporte.getIncidenciasPorSeveridad()
                    .forEach((k, v) -> pw.println(k + "," + v));
        }

        pw.flush();
        return baos.toByteArray();
    }

    // ===================== Helpers privados =====================

    private List<Estudiante> obtenerEstudiantes(ReporteRequestDTO req) {
        if (req.getEstudianteId() != null) {
            return estudianteRepository.findByIdAndActivoTrue(req.getEstudianteId())
                    .map(List::of).orElse(List.of());
        }
        List<Estudiante> todos = estudianteRepository.findByActivoTrue();
        return todos.stream()
                .filter(e -> req.getCarrera() == null
                        || req.getCarrera().equalsIgnoreCase(e.getCarrera()))
                .filter(e -> req.getGrupo() == null
                        || req.getGrupo().equalsIgnoreCase(e.getGrupo()))
                .collect(Collectors.toList());
    }

    private List<Instancia> obtenerInstancias(ReporteRequestDTO req,
                                               List<Estudiante> estudiantes) {
        Set<Long> ids = estudiantes.stream()
                .map(Estudiante::getId).collect(Collectors.toSet());

        return instanciaRepository.findByActivoTrueOrderByFechaInstanciaDesc()
                .stream()
                .filter(i -> ids.contains(i.getEstudiante().getId()))
                .filter(i -> req.getFechaInicio() == null
                        || !i.getFechaInstancia().toLocalDate().isBefore(req.getFechaInicio()))
                .filter(i -> req.getFechaFin() == null
                        || !i.getFechaInstancia().toLocalDate().isAfter(req.getFechaFin()))
                .collect(Collectors.toList());
    }

    private List<Recordatorio> obtenerRecordatorios(ReporteRequestDTO req,
                                                     List<Estudiante> estudiantes) {
        Set<Long> ids = estudiantes.stream()
                .map(Estudiante::getId).collect(Collectors.toSet());

        return recordatorioRepository.findByActivoTrueOrderByFechaRecordatorioAsc()
                .stream()
                .filter(r -> ids.contains(r.getEstudiante().getId()))
                .filter(r -> req.getFechaInicio() == null
                        || !r.getFechaRecordatorio().toLocalDate().isBefore(req.getFechaInicio()))
                .filter(r -> req.getFechaFin() == null
                        || !r.getFechaRecordatorio().toLocalDate().isAfter(req.getFechaFin()))
                .collect(Collectors.toList());
    }

    private List<Incidencia> obtenerIncidencias(ReporteRequestDTO req,
                                                 List<Estudiante> estudiantes) {
        Set<Long> ids = estudiantes.stream()
                .map(Estudiante::getId).collect(Collectors.toSet());

        return incidenciaRepository.findByActivoTrueOrderByFechaIncidenciaDesc()
                .stream()
                .filter(i -> ids.contains(i.getEstudiante().getId()))
                .filter(i -> req.getFechaInicio() == null
                        || !i.getFechaIncidencia().toLocalDate().isBefore(req.getFechaInicio()))
                .filter(i -> req.getFechaFin() == null
                        || !i.getFechaIncidencia().toLocalDate().isAfter(req.getFechaFin()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> construirDetalleEstudiante(
            Estudiante e, List<Instancia> instancias,
            List<Recordatorio> recordatorios, List<Incidencia> incidencias) {

        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("id", e.getId());
        detalle.put("cedula", e.getCedula());
        detalle.put("nombre", e.getNombre() + " " + e.getApellido());
        detalle.put("carrera", e.getCarrera());
        detalle.put("grupo", e.getGrupo());
        detalle.put("totalInstancias", instancias.size());
        detalle.put("totalRecordatorios", recordatorios.size());
        detalle.put("totalIncidencias", incidencias.size());
        detalle.put("incidenciasAbiertas",
                incidencias.stream().filter(i -> "ABIERTA".equals(i.getEstado())).count());

        return List.of(detalle);
    }

    private String construirTitulo(ReporteRequestDTO req) {
        StringBuilder sb = new StringBuilder("Reporte SIENEP");
        if (req.getCarrera() != null) sb.append(" — Carrera: ").append(req.getCarrera());
        if (req.getGrupo() != null) sb.append(" — Grupo: ").append(req.getGrupo());
        if (req.getEstudianteId() != null) sb.append(" — Estudiante ID: ").append(req.getEstudianteId());
        if (req.getFechaInicio() != null) sb.append(" — Desde: ").append(req.getFechaInicio());
        if (req.getFechaFin() != null) sb.append(" — Hasta: ").append(req.getFechaFin());
        return sb.toString();
    }
}
