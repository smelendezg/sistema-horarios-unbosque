package co.edu.unbosque.horarios.rules;
import org.springframework.stereotype.Component; import java.util.Optional;
@Component public class ClassroomCapacityRule implements ScheduleRule {
 public Optional<ValidationIssue> validate(AssignmentCandidate c){
  var course=c.seccion().getCurso(); var room=c.aula();
  if(room.getCapacidad()<c.seccion().getEstudiantesEstimados()) return error("CAPACIDAD","El aula no tiene capacidad suficiente para la sección.");
  if(course.isRequiereComputadores()&&!room.isComputadores()) return error("EQUIPO","El curso requiere computadores.");
  if(course.isRequiereSillasMoviles()&&!room.isSillasMoviles()) return error("MOBILIARIO","El curso requiere sillas móviles.");
  return Optional.empty();
 }
 private Optional<ValidationIssue> error(String c,String m){return Optional.of(new ValidationIssue(ValidationIssue.Level.ERROR,c,m));}
}
