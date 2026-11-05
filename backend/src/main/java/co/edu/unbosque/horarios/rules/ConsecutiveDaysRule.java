package co.edu.unbosque.horarios.rules;
import org.springframework.stereotype.Component; import java.util.Optional;
@Component public class ConsecutiveDaysRule implements ScheduleRule {
 public Optional<ValidationIssue> validate(AssignmentCandidate c){
  if(c.seccion().getCurso().getFrecuenciaSemanal()<2||c.seccion().getCurso().getFrecuenciaSemanal()>3)return Optional.empty();
  boolean consecutive=c.existentes().stream().anyMatch(s->s.getSeccion().getId().equals(c.seccion().getId())&&Math.abs(s.getDiaSemana()-c.dia())==1);
  return consecutive?Optional.of(new ValidationIssue(ValidationIssue.Level.ADVERTENCIA,"DIAS_CONSECUTIVOS","Las sesiones del curso quedaron en días consecutivos.")):Optional.empty();
 }
}
