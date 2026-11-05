package co.edu.unbosque.horarios.api;

import co.edu.unbosque.horarios.rules.ValidationIssue;
import jakarta.validation.constraints.*;
import java.time.LocalTime;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ApiDtos {
  private ApiDtos() {}

  public record LoginRequest(@Email @NotBlank String correo, @NotBlank String clave) {}
  public record LoginResponse(String token, String nombre, String correo, String rol) {}
  public record AssignRequest(@NotNull Long seccionId, @NotNull Long aulaId,
                              @Min(1) @Max(6) int dia, @NotNull LocalTime horaInicio) {}
  public record MoveSessionRequest(@NotNull Long aulaId, @Min(1) @Max(6) int dia,
                                   @NotNull LocalTime horaInicio) {}
  public record ReviewRequest(@NotBlank String decision, @Size(max = 1000) String observaciones) {}
  public record SuggestionRequest(@NotNull Long periodoId, @NotNull Long cursoId,
                                  @Min(1) int demanda, @NotNull Long docenteId) {}
  public record Suggestion(int minimo, int recomendado, double promedioMinimo,
                           double promedioRecomendado) {}
  public record SessionView(Long id, Long sectionId, String courseCode, String courseName,
                            int sectionNumber, String teacher, String classroom, int day,
                            String start, String end) {}
  public record SectionView(Long id, String courseCode, String courseName, int number,
                            String teacher, int estimated, int weeklyFrequency,
                            int assignedSessions) {}
  public record ScheduleView(Long id, String period, int version, String status,
                             List<SessionView> sessions, List<SectionView> unassigned) {}
  public record AssignmentResult(SessionView session, List<ValidationIssue> issues) {}

  public record CourseInput(@NotBlank String codigo, @NotBlank String nombre,
                            @Min(1) int creditos, @Min(1) @Max(4) int frecuenciaSemanal,
                            boolean requiereComputadores, boolean requiereSillasMoviles) {}
  public record TeacherInput(String idExterno, @NotBlank String nombre, @Email @NotBlank String correo,
                             @NotBlank String vinculacion, @NotBlank String escalafon) {}
  public record ClassroomInput(@NotBlank String codigo, @NotBlank String nombre,
                               @Min(1) int capacidad, boolean computadores,
                               boolean sillasMoviles) {}
  public record SectionInput(@NotNull Long cursoId, @NotNull Long periodoId,
                             @NotNull Long docenteId, @Min(1) int numero,
                             @Min(0) int estudiantesEstimados) {}
  public record ParameterInput(@NotBlank String valor) {}
  public record ReviewView(Long id, String decision, String observaciones,
                           Instant fecha, String usuario) {}
  public record UserInput(@NotBlank String nombre,
                          @Email @Pattern(regexp=".+@unbosque\\.edu\\.co$",message="Debe usar correo institucional") String correo,
                          @Size(min=8) String clave,@NotBlank String rol) {}
  public record RoleInput(@NotBlank String rol) {}
  public record StatusInput(boolean activo) {}
  public record UserView(Long id,String nombre,String correo,String rol,boolean activo) {}
  public record AvailabilityInput(@Min(1) @Max(6) int dia,@NotNull LocalTime horaInicio,
                                  @NotNull LocalTime horaFin) {}
  public record AvailabilityView(Long id,int dia,LocalTime horaInicio,LocalTime horaFin) {}
  public record EnrollmentView(Long id,Long periodoId,String periodo,Long cursoId,String codigoCurso,
                               String curso,int demanda,int minimo,int recomendado,LocalDate fechaImportacion) {}
  public record ImportResult(int procesados,int correctos,List<String> errores) {}
}
