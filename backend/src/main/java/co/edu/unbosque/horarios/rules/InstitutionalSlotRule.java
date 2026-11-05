package co.edu.unbosque.horarios.rules;
import co.edu.unbosque.horarios.service.ParameterService; import org.springframework.stereotype.Component; import java.time.LocalTime; import java.util.Optional;
@Component public class InstitutionalSlotRule implements ScheduleRule {
 private final ParameterService parameters; public InstitutionalSlotRule(ParameterService parameters){this.parameters=parameters;}
 public Optional<ValidationIssue> validate(AssignmentCandidate c){
  var end=c.fin(); boolean lunch=c.inicio().isBefore(LocalTime.of(13,0))&&end.isAfter(LocalTime.NOON);
  var start=parameters.time("hora_inicio_semana",LocalTime.of(7,0));var finish=c.dia()==6?parameters.time("hora_fin_sabado",LocalTime.of(13,0)):parameters.time("hora_fin_semana",LocalTime.of(22,0));
  boolean range=c.dia()>=1&&c.dia()<=6&&!c.inicio().isBefore(start)&&!end.isAfter(finish);
  return range&&!lunch?Optional.empty():Optional.of(new ValidationIssue(ValidationIssue.Level.ERROR,"FRANJA","La sesión no cumple la franja institucional o cruza el descanso de 12:00 a 13:00."));
 }
}
