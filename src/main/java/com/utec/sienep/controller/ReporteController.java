package com.utec.sienep.controller;

import com.utec.sienep.dto.request.ReporteRequestDTO;
import com.utec.sienep.dto.response.ApiResponseDTO;
import com.utec.sienep.dto.response.ReporteResponseDTO;
import com.utec.sienep.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "Generación y exportación de reportes del sistema SIENEP (RF30-RF31)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','DOCENTE','PSICOPEDAGOGO','DIRECCION')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // ===================== RF30 – Generación de Reporte (JSON) =====================

    @GetMapping
    @Operation(summary = "Generar reporte (RF30)",
        description = "Genera un reporte con métricas de instancias, recordatorios e incidencias. " +
                      "Filtros opcionales: fechaInicio, fechaFin, carrera, grupo, estudianteId.")
    public ResponseEntity<ApiResponseDTO<ReporteResponseDTO>> generar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) Long estudianteId) {

        ReporteRequestDTO req = buildRequest(fechaInicio, fechaFin, carrera, grupo, estudianteId, "JSON");
        ReporteResponseDTO reporte = reporteService.generar(req);
        return ResponseEntity.ok(ApiResponseDTO.ok("Reporte generado.", reporte));
    }

    // ===================== RF31 – Exportación CSV =====================

    @GetMapping("/exportar/csv")
    @Operation(summary = "Exportar reporte a CSV (RF31)",
        description = "Exporta el reporte en formato CSV. Acepta los mismos filtros que el endpoint de generación.")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) Long estudianteId) {

        ReporteRequestDTO req = buildRequest(fechaInicio, fechaFin, carrera, grupo, estudianteId, "CSV");
        byte[] csv = reporteService.exportarCsv(req);

        String nombreArchivo = "reporte_sienep_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    // ===================== Helper =====================

    private ReporteRequestDTO buildRequest(LocalDate fechaInicio, LocalDate fechaFin,
                                           String carrera, String grupo,
                                           Long estudianteId, String formato) {
        ReporteRequestDTO req = new ReporteRequestDTO();
        req.setFechaInicio(fechaInicio);
        req.setFechaFin(fechaFin);
        req.setCarrera(carrera);
        req.setGrupo(grupo);
        req.setEstudianteId(estudianteId);
        req.setFormato(formato);
        return req;
    }
}
