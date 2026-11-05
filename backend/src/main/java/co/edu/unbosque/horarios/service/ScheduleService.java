package co.edu.unbosque.horarios.service;

import co.edu.unbosque.horarios.api.ApiDtos;
import co.edu.unbosque.horarios.domain.*;
import co.edu.unbosque.horarios.repository.*;
import co.edu.unbosque.horarios.rules.*;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ScheduleService {
  private final ScheduleRepository schedules;
  private final SectionRepository sections;
  private final ClassroomRepository rooms;
  private final ClassSessionRepository sessions;
  private final RulesEngine rules;
  private final UserRepository users;
  private final ScheduleReviewRepository reviews;
  private final JdbcTemplate jdbc;
  private final ParameterService parameters;

  public ScheduleService(ScheduleRepository schedules, SectionRepository sections,
                         ClassroomRepository rooms, ClassSessionRepository sessions,
                         RulesEngine rules, UserRepository users,
                         ScheduleReviewRepository reviews, JdbcTemplate jdbc, ParameterService parameters) {
    this.schedules = schedules;
    this.sections = sections;
    this.rooms = rooms;
    this.sessions = sessions;
    this.rules = rules;
    this.users = users;
    this.reviews = reviews;
    this.jdbc = jdbc;
    this.parameters = parameters;
  }

  @Transactional
  public ApiDtos.ScheduleView get(Long id) {
    var schedule = schedules.findById(id).orElseThrow(() -> notFound("Horario"));
    var allSections = sections.findByPeriodoId(schedule.getPeriodo().getId());
    var counts = schedule.getSesiones().stream().collect(Collectors.groupingBy(
        s -> s.getSeccion().getId(), Collectors.counting()));
    var pending = allSections.stream()
        .filter(section -> counts.getOrDefault(section.getId(), 0L)
            < section.getCurso().getFrecuenciaSemanal())
        .toList();
    return view(schedule, pending, counts);
  }

  @Transactional
  public ApiDtos.ScheduleView current() {
    return schedules.findAll().stream().max(Comparator.comparing(Schedule::getId))
        .map(schedule -> get(schedule.getId())).orElseThrow(() -> notFound("Horario"));
  }

  @Transactional
  public ApiDtos.ScheduleView createNextVersion() {
    var current = schedules.findAll().stream().max(Comparator.comparing(Schedule::getId))
        .orElseThrow(() -> notFound("Horario"));
    if (current.getEstado() != ScheduleStatus.PUBLICADO) {
      throw conflict("Publica el horario actual antes de crear una nueva versión.");
    }
    int nextVersion = schedules.findAll().stream()
        .filter(schedule -> schedule.getPeriodo().getId().equals(current.getPeriodo().getId()))
        .mapToInt(Schedule::getVersion)
        .max()
        .orElse(0) + 1;
    var next = schedules.save(new Schedule(current.getPeriodo(), nextVersion));
    audit(next, "HORARIO", "CREAR_VERSION", "Versión " + nextVersion);
    return get(next.getId());
  }

  @Transactional
  public ApiDtos.ScheduleView published() {
    var schedule = schedules.findFirstByEstadoOrderByIdDesc(ScheduleStatus.PUBLICADO)
        .orElseThrow(() -> notFound("Horario publicado"));
    return get(schedule.getId());
  }

  @Transactional
  public ApiDtos.AssignmentResult assign(Long id, ApiDtos.AssignRequest request) {
    var schedule = editableSchedule(id);
    var section = sections.findById(request.seccionId()).orElseThrow(() -> notFound("Sección"));
    var room = rooms.findById(request.aulaId()).orElseThrow(() -> notFound("Aula"));
    requireSamePeriod(schedule, section);
    var existing = sessions.findByHorarioId(id);
    long assigned = existing.stream().filter(s -> s.getSeccion().getId().equals(section.getId())).count();
    if (assigned >= section.getCurso().getFrecuenciaSemanal())
      throw conflict("La sección ya completó su frecuencia semanal.");
    if (existing.stream().anyMatch(s -> s.getSeccion().getId().equals(section.getId())
        && s.getDiaSemana() == request.dia()))
      throw conflict("La sección ya tiene una sesión asignada ese día.");
    int duration=parameters.integer("duracion_sesion",2);
    var issues = rules.validate(new AssignmentCandidate(schedule, section, room,
        request.dia(), request.horaInicio(), duration, existing, null));
    if (hasErrors(issues)) return new ApiDtos.AssignmentResult(null, issues);
    var saved = sessions.save(new ClassSession(schedule, section, room, request.dia(), request.horaInicio(),duration));
    audit(schedule, "SESION", "CREAR", section.getCurso().getCodigo() + " · " + request.horaInicio());
    return new ApiDtos.AssignmentResult(sessionView(saved), issues);
  }

  @Transactional
  public ApiDtos.AssignmentResult move(Long scheduleId, Long sessionId, ApiDtos.MoveSessionRequest request) {
    var schedule = editableSchedule(scheduleId);
    var session = sessions.findById(sessionId).orElseThrow(() -> notFound("Sesión"));
    if (!session.getHorario().getId().equals(scheduleId)) throw notFound("Sesión");
    var room = rooms.findById(request.aulaId()).orElseThrow(() -> notFound("Aula"));
    var existing = sessions.findByHorarioId(scheduleId);
    if (existing.stream().anyMatch(s -> !s.getId().equals(sessionId)
        && s.getSeccion().getId().equals(session.getSeccion().getId())
        && s.getDiaSemana() == request.dia()))
      throw conflict("La sección ya tiene otra sesión asignada ese día.");
    int duration=parameters.integer("duracion_sesion",2);
    var issues = rules.validate(new AssignmentCandidate(schedule, session.getSeccion(), room,
        request.dia(), request.horaInicio(), duration, existing, sessionId));
    if (hasErrors(issues)) return new ApiDtos.AssignmentResult(null, issues);
    session.move(room, request.dia(), request.horaInicio(),duration);
    audit(schedule, "SESION", "MOVER", session.getSeccion().getCurso().getCodigo());
    return new ApiDtos.AssignmentResult(sessionView(session), issues);
  }

  @Transactional
  public void deleteSession(Long scheduleId, Long sessionId) {
    var schedule = editableSchedule(scheduleId);
    var session = sessions.findById(sessionId).orElseThrow(() -> notFound("Sesión"));
    if (!session.getHorario().getId().equals(scheduleId)) throw notFound("Sesión");
    sessions.delete(session);
    audit(schedule, "SESION", "ELIMINAR", session.getSeccion().getCurso().getCodigo());
  }

  @Transactional
  public ApiDtos.ScheduleView transition(Long id, ScheduleStatus next) {
    var schedule = schedules.findById(id).orElseThrow(() -> notFound("Horario"));
    boolean valid = switch (schedule.getEstado()) {
      case BORRADOR -> next == ScheduleStatus.EN_REVISION;
      case EN_REVISION -> next == ScheduleStatus.APROBADO || next == ScheduleStatus.BORRADOR;
      case APROBADO -> next == ScheduleStatus.PUBLICADO;
      case PUBLICADO -> false;
    };
    if (!valid) throw conflict("Transición de estado no permitida.");
    if (next == ScheduleStatus.EN_REVISION || next == ScheduleStatus.APROBADO
        || next == ScheduleStatus.PUBLICADO) requireComplete(schedule);
    schedule.setEstado(next);
    audit(schedule, "HORARIO", next.name(), "Cambio de estado");
    return get(id);
  }

  @Transactional
  public ApiDtos.ScheduleView review(Long id, ApiDtos.ReviewRequest request, String email) {
    var schedule = schedules.findById(id).orElseThrow(() -> notFound("Horario"));
    var user = users.findByCorreo(email).orElseThrow(() -> notFound("Usuario"));
    String decision = request.decision().toUpperCase(Locale.ROOT);
    ScheduleStatus next;
    if (decision.equals("APROBADO")) next = ScheduleStatus.APROBADO;
    else if (decision.equals("DEVUELTO")) {
      if (request.observaciones() == null || request.observaciones().isBlank())
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Escribe una observación para devolver el horario.");
      next = ScheduleStatus.BORRADOR;
    } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decisión no válida.");
    var result = transition(id, next);
    reviews.save(new ScheduleReview(schedule, user, decision, request.observaciones()));
    return result;
  }

  public ApiDtos.SessionView sessionView(ClassSession session) {
    var section = session.getSeccion();
    return new ApiDtos.SessionView(session.getId(), section.getId(),
        section.getCurso().getCodigo(), section.getCurso().getNombre(), section.getNumero(),
        section.getDocente() == null ? "Por asignar" : section.getDocente().getNombre(),
        session.getAula().getCodigo(), session.getDiaSemana(),
        session.getHoraInicio().toString(), session.getHoraFin().toString());
  }

  private ApiDtos.ScheduleView view(Schedule schedule, List<Section> pending, Map<Long, Long> counts) {
    return new ApiDtos.ScheduleView(schedule.getId(), schedule.getPeriodo().getCodigo(),
        schedule.getVersion(), schedule.getEstado().name(),
        schedule.getSesiones().stream().map(this::sessionView).toList(),
        pending.stream().map(s -> new ApiDtos.SectionView(s.getId(), s.getCurso().getCodigo(),
            s.getCurso().getNombre(), s.getNumero(),
            s.getDocente() == null ? "Por asignar" : s.getDocente().getNombre(),
            s.getEstudiantesEstimados(), s.getCurso().getFrecuenciaSemanal(),
            counts.getOrDefault(s.getId(), 0L).intValue())).toList());
  }

  private Schedule editableSchedule(Long id) {
    var schedule = schedules.findById(id).orElseThrow(() -> notFound("Horario"));
    if (schedule.getEstado() != ScheduleStatus.BORRADOR)
      throw conflict("Solo se puede editar un horario en borrador.");
    return schedule;
  }

  private void requireSamePeriod(Schedule schedule, Section section) {
    if (!schedule.getPeriodo().getId().equals(section.getPeriodo().getId()))
      throw conflict("La sección pertenece a un periodo académico diferente.");
  }

  private void requireComplete(Schedule schedule) {
    var allSections = sections.findByPeriodoId(schedule.getPeriodo().getId());
    var counts = sessions.findByHorarioId(schedule.getId()).stream().collect(Collectors.groupingBy(
        s -> s.getSeccion().getId(), Collectors.counting()));
    var incomplete = allSections.stream()
        .filter(s -> counts.getOrDefault(s.getId(), 0L) < s.getCurso().getFrecuenciaSemanal())
        .map(s -> s.getCurso().getCodigo() + " G" + s.getNumero()).toList();
    if (!incomplete.isEmpty()) throw conflict("Completa las sesiones pendientes: " + String.join(", ", incomplete));
  }

  private boolean hasErrors(List<ValidationIssue> issues) {
    return issues.stream().anyMatch(i -> i.nivel() == ValidationIssue.Level.ERROR);
  }

  private void audit(Schedule schedule, String entity, String action, String detail) {
    Long userId = null;
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated())
      userId = users.findByCorreo(authentication.getName()).map(User::getId).orElse(null);
    jdbc.update("insert into bitacora(id_usuario,id_horario,entidad,accion,detalle) values (?,?,?,?,?)",
        userId, schedule.getId(), entity, action, detail);
  }

  private ResponseStatusException notFound(String name) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, name + " no encontrado.");
  }

  private ResponseStatusException conflict(String message) {
    return new ResponseStatusException(HttpStatus.CONFLICT, message);
  }
}
