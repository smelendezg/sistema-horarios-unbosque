package co.edu.unbosque.horarios.rules;
import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Component; import java.sql.Time; import java.util.Optional;
@Component public class TeacherEligibilityRule implements ScheduleRule {
 private final JdbcTemplate jdbc; public TeacherEligibilityRule(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public Optional<ValidationIssue> validate(AssignmentCandidate c){
  if(c.seccion().getDocente()==null)return error("DOCENTE","La sección todavía no tiene docente asignado.");
  int qualified=jdbc.queryForObject("select count(*) from docente_curso where id_docente=? and id_curso=?",Integer.class,c.seccion().getDocente().getId(),c.seccion().getCurso().getId());
  if(qualified==0)return error("DOCENTE_CURSO","El docente no está habilitado para orientar este curso.");
  int available=jdbc.queryForObject("select count(*) from disponibilidad_docente where id_docente=? and dia_semana=? and hora_inicio<=? and hora_fin>=?",Integer.class,c.seccion().getDocente().getId(),c.dia(),Time.valueOf(c.inicio()),Time.valueOf(c.fin()));
  return available==0?error("DISPONIBILIDAD","La franja está fuera de la disponibilidad declarada por el docente."):Optional.empty();
 }
 private Optional<ValidationIssue> error(String code,String message){return Optional.of(new ValidationIssue(ValidationIssue.Level.ERROR,code,message));}
}
