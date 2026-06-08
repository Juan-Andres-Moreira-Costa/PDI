package com.utec.sienep.service;

import com.utec.sienep.dto.request.InstanciaRequestDTO;
import com.utec.sienep.dto.request.RecordatorioRequestDTO;
import com.utec.sienep.dto.response.InstanciaResponseDTO;
import com.utec.sienep.dto.response.RecordatorioResponseDTO;
import com.utec.sienep.entity.*;
import com.utec.sienep.exception.RecursoNoEncontradoException;
import com.utec.sienep.exception.ReglaNegocioException;
import com.utec.sienep.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecordatorioService {

    private final RecordatorioRepository recordatorioRepository;
    private final EstudianteRepository estudianteRepository;
    private final CategoriaRecordatorioRepository categoriaRepository;
    private final InstanciaRepository instanciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final InstanciaService instanciaService;

    public RecordatorioService(RecordatorioRepository recordatorioRepository,
                               EstudianteRepository estudianteRepository,
                               CategoriaRecordatorioRepository categoriaRepository,
                               InstanciaRepository instanciaRepository,
                               UsuarioRepository usuarioRepository,
                               AuditoriaService auditoriaService,
                               InstanciaService instanciaService) {
        this.recordatorioRepository = recordatorioRepository;
        this.estudianteRepository = estudianteRepository;
        this.categoriaRepository = categoriaRepository;
        this.instanciaRepository = instanciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.instanciaService = instanciaService;
    }

    // ===================== RF19 – Creación de Recordatorio =====================

    @Transactional
    public RecordatorioResponseDTO crear(RecordatorioRequestDTO dto) {
        Estudiante estudiante = estudianteRepository.findByIdAndActivoTrue(dto.getEstudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Estudiante no encontrado: " + dto.getEstudianteId()));

        // Validar frecuencia si es recurrente
        if (dto.isEsRecurrente()) {
            validarFrecuencia(dto.getFrecuenciaRecurrencia());
            if (dto.getFechaFinRecurrencia() == null) {
                throw new ReglaNegocioException(
                        "Los recordatorios recurrentes requieren una fecha de fin de recurrencia.");
            }
            if (dto.getFechaFinRecurrencia().isBefore(dto.getFechaRecordatorio())) {
                throw new ReglaNegocioException(
                        "La fecha de fin de recurrencia debe ser posterior a la fecha del recordatorio.");
            }
        }

        Recordatorio rec = construirDesdeDTO(dto, estudiante, null);
        Recordatorio guardado = recordatorioRepository.save(rec);

        // RF20 – Si es recurrente, generar las instancias de la serie
        if (dto.isEsRecurrente()) {
            generarSerieRecurrente(guardado, dto);
        }

        String username = getUsername();
        auditoriaService.registrarExitoso(username, "ALTA_RECORDATORIO",
                "Recordatorio", guardado.getId(),
                "Recordatorio " + guardado.getIdentificador() + " creado");

        return mapearEntidadADto(guardado);
    }

    // ===================== RF26 – Visualización =====================

    @Transactional(readOnly = true)
    public List<RecordatorioResponseDTO> listarTodos() {
        return recordatorioRepository.findByActivoTrueOrderByFechaRecordatorioAsc()
                .stream().map(this::mapearEntidadADto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecordatorioResponseDTO> listarPorEstudiante(Long estudianteId) {
        if (!estudianteRepository.existsById(estudianteId)) {
            throw new RecursoNoEncontradoException(
                    "Estudiante no encontrado: " + estudianteId);
        }
        return recordatorioRepository
                .findByEstudianteIdAndActivoTrueOrderByFechaRecordatorioAsc(estudianteId)
                .stream().map(this::mapearEntidadADto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecordatorioResponseDTO buscarPorId(Long id) {
        return mapearEntidadADto(
                recordatorioRepository.findByIdAndActivoTrue(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Recordatorio no encontrado: " + id)));
    }

    // ===================== RF25 – Gestión (modificar) =====================

    @Transactional
    public RecordatorioResponseDTO modificar(Long id, RecordatorioRequestDTO dto) {
        Recordatorio rec = recordatorioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Recordatorio no encontrado: " + id));

        rec.setTitulo(dto.getTitulo());
        rec.setDescripcion(dto.getDescripcion());
        rec.setFechaRecordatorio(dto.getFechaRecordatorio());
        rec.setTipo(dto.getTipo() != null ? dto.getTipo() : "GENERAL");
        rec.setFechaModificacion(LocalDateTime.now());

        if (dto.getCategoriaId() != null) {
            CategoriaRecordatorio cat = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Categoría no encontrada: " + dto.getCategoriaId()));
            rec.setCategoria(cat);
        }

        Recordatorio actualizado = recordatorioRepository.save(rec);

        auditoriaService.registrarExitoso(getUsername(), "MODIFICACION_RECORDATORIO",
                "Recordatorio", id,
                "Recordatorio " + rec.getIdentificador() + " modificado");

        return mapearEntidadADto(actualizado);
    }

    // ===================== Baja lógica =====================

    @Transactional
    public void darDeBaja(Long id) {
        Recordatorio rec = recordatorioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Recordatorio no encontrado: " + id));

        rec.setActivo(false);
        rec.setEstado("CANCELADO");
        recordatorioRepository.save(rec);

        auditoriaService.registrarExitoso(getUsername(), "BAJA_RECORDATORIO",
                "Recordatorio", id,
                "Recordatorio " + rec.getIdentificador() + " cancelado");
    }

    // ===================== RF27 – Crear Instancia desde Recordatorio =====================

    @Transactional
    public InstanciaResponseDTO crearInstanciaDesdeRecordatorio(Long recordatorioId) {
        Recordatorio rec = recordatorioRepository.findByIdAndActivoTrue(recordatorioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Recordatorio no encontrado: " + recordatorioId));

        if (rec.getInstanciaGenerada() != null) {
            throw new ReglaNegocioException(
                    "Este recordatorio ya tiene una instancia generada: "
                    + rec.getInstanciaGenerada().getIdentificador());
        }

        InstanciaRequestDTO instDto = new InstanciaRequestDTO();
        instDto.setEstudianteId(rec.getEstudiante().getId());
        instDto.setTitulo("Instancia generada desde: " + rec.getTitulo());
        instDto.setDescripcion(rec.getDescripcion());
        instDto.setFechaInstancia(rec.getFechaRecordatorio());

        InstanciaResponseDTO instanciaDTO = instanciaService.crear(instDto);

        // Vincular la instancia creada al recordatorio
        instanciaRepository.findById(instanciaDTO.getId()).ifPresent(inst -> {
            rec.setInstanciaGenerada(inst);
            rec.setEstado("COMPLETADO");
            recordatorioRepository.save(rec);
        });

        auditoriaService.registrarExitoso(getUsername(), "INSTANCIA_DESDE_RECORDATORIO",
                "Recordatorio", recordatorioId,
                "Instancia " + instanciaDTO.getIdentificador()
                + " creada desde recordatorio " + rec.getIdentificador());

        return instanciaDTO;
    }

    // ===================== RF23 – Simular notificación =====================
    // En producción esto se dispararía con @Scheduled; aquí se expone como endpoint
    // para poder demostrarlo en Swagger.

    @Transactional
    public List<RecordatorioResponseDTO> procesarNotificacionesPendientes() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusHours(24);

        List<Recordatorio> pendientes =
                recordatorioRepository.findPendientesDeNotificacion(ahora, limite);

        List<RecordatorioResponseDTO> notificados = new ArrayList<>();
        for (Recordatorio r : pendientes) {
            r.setNotificacionEnviada(true);
            r.setFechaNotificacion(LocalDateTime.now());
            recordatorioRepository.save(r);
            notificados.add(mapearEntidadADto(r));
            // En producción: enviar email / push notification aquí
        }
        return notificados;
    }

    // ===================== RF20 – Generación de serie recurrente =====================

    private void generarSerieRecurrente(Recordatorio padre, RecordatorioRequestDTO dto) {
        LocalDateTime siguiente = calcularSiguienteFecha(
                dto.getFechaRecordatorio(), dto.getFrecuenciaRecurrencia());

        while (siguiente != null
                && !siguiente.isAfter(dto.getFechaFinRecurrencia())) {

            RecordatorioRequestDTO hijoDto = new RecordatorioRequestDTO();
            hijoDto.setEstudianteId(dto.getEstudianteId());
            hijoDto.setCategoriaId(dto.getCategoriaId());
            hijoDto.setTitulo(dto.getTitulo());
            hijoDto.setDescripcion(dto.getDescripcion());
            hijoDto.setFechaRecordatorio(siguiente);
            hijoDto.setTipo(dto.getTipo());
            hijoDto.setEsRecurrente(false); // Los hijos no generan nueva serie

            Recordatorio hijo = construirDesdeDTO(hijoDto,
                    padre.getEstudiante(), padre);
            recordatorioRepository.save(hijo);

            siguiente = calcularSiguienteFecha(siguiente, dto.getFrecuenciaRecurrencia());
        }
    }

    private LocalDateTime calcularSiguienteFecha(LocalDateTime actual, String frecuencia) {
        if (frecuencia == null) return null;
        return switch (frecuencia.toUpperCase()) {
            case "DIARIA"     -> actual.plusDays(1);
            case "SEMANAL"    -> actual.plusWeeks(1);
            case "QUINCENAL"  -> actual.plusWeeks(2);
            case "MENSUAL"    -> actual.plusMonths(1);
            default           -> null;
        };
    }

    private void validarFrecuencia(String frecuencia) {
        if (frecuencia == null) {
            throw new ReglaNegocioException(
                    "Debe especificar la frecuencia de recurrencia: DIARIA, SEMANAL, QUINCENAL o MENSUAL.");
        }
        List<String> validas = List.of("DIARIA", "SEMANAL", "QUINCENAL", "MENSUAL");
        if (!validas.contains(frecuencia.toUpperCase())) {
            throw new ReglaNegocioException(
                    "Frecuencia inválida. Valores aceptados: " + validas);
        }
    }

    // ===================== RF24 – Generación de identificador =====================
    // Formato: REC-YYYYMMDD-XXXX

    private String generarIdentificador() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefijo = "REC-" + fecha + "-";
        long count = recordatorioRepository.countByIdentificadorStartingWith(prefijo);
        return prefijo + String.format("%04d", count + 1);
    }

    // ===================== Helpers =====================

    private Recordatorio construirDesdeDTO(RecordatorioRequestDTO dto,
                                           Estudiante estudiante,
                                           Recordatorio padre) {
        Recordatorio rec = new Recordatorio();
        rec.setIdentificador(generarIdentificador());
        rec.setEstudiante(estudiante);
        rec.setTitulo(dto.getTitulo().trim());
        rec.setDescripcion(dto.getDescripcion());
        rec.setFechaRecordatorio(dto.getFechaRecordatorio());
        rec.setEsRecurrente(dto.isEsRecurrente());
        rec.setFrecuenciaRecurrencia(dto.getFrecuenciaRecurrencia());
        rec.setFechaFinRecurrencia(dto.getFechaFinRecurrencia());
        rec.setTipo(dto.getTipo() != null ? dto.getTipo() : "GENERAL");
        rec.setEstado("PENDIENTE");
        rec.setActivo(true);
        rec.setFechaAlta(LocalDateTime.now());
        rec.setRecordatorioPadre(padre);

        if (dto.getCategoriaId() != null) {
            categoriaRepository.findById(dto.getCategoriaId())
                    .ifPresent(rec::setCategoria);
        }

        String username = getUsername();
        usuarioRepository.findByUsernameAndActivoTrue(username)
                .ifPresent(rec::setCreadoPor);

        return rec;
    }

    private String getUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "sistema";
        }
    }

    private RecordatorioResponseDTO mapearEntidadADto(Recordatorio r) {
        RecordatorioResponseDTO dto = new RecordatorioResponseDTO();
        dto.setId(r.getId());
        dto.setIdentificador(r.getIdentificador());
        dto.setEstudianteId(r.getEstudiante().getId());
        dto.setEstudianteNombre(r.getEstudiante().getNombre() + " " + r.getEstudiante().getApellido());
        dto.setEstudianteCedula(r.getEstudiante().getCedula());
        if (r.getCategoria() != null) {
            dto.setCategoriaId(r.getCategoria().getId());
            dto.setCategoriaNombre(r.getCategoria().getNombre());
        }
        dto.setTitulo(r.getTitulo());
        dto.setDescripcion(r.getDescripcion());
        dto.setFechaRecordatorio(r.getFechaRecordatorio());
        dto.setEsRecurrente(r.isEsRecurrente());
        dto.setFrecuenciaRecurrencia(r.getFrecuenciaRecurrencia());
        dto.setFechaFinRecurrencia(r.getFechaFinRecurrencia());
        dto.setTipo(r.getTipo());
        dto.setGoogleCalendarEventId(r.getGoogleCalendarEventId());
        dto.setNotificacionEnviada(r.isNotificacionEnviada());
        dto.setFechaNotificacion(r.getFechaNotificacion());
        dto.setEstado(r.getEstado());
        dto.setActivo(r.isActivo());
        dto.setFechaAlta(r.getFechaAlta());
        dto.setFechaModificacion(r.getFechaModificacion());
        if (r.getCreadoPor() != null) dto.setCreadoPorUsername(r.getCreadoPor().getUsername());
        if (r.getInstanciaGenerada() != null) {
            dto.setInstanciaGeneradaId(r.getInstanciaGenerada().getId());
            dto.setInstanciaGeneradaIdentificador(r.getInstanciaGenerada().getIdentificador());
        }
        if (r.getRecordatorioPadre() != null) {
            dto.setRecordatorioPadreId(r.getRecordatorioPadre().getId());
        }
        return dto;
    }
}
