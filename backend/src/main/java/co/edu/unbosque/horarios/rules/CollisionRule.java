package co.edu.unbosque.horarios.rules;
import org.springframework.stereotype.Component; import java.util.Optional;
@Component public class CollisionRule implements ScheduleRule {
 public Optional<ValidationIssue> validate(AssignmentCandidate c){
  for(var s:c.existentes()){
   if(s.getId().equals(c.sesionEditada())||s.getDiaSemana()!=c.dia()||!c.overlaps(s)) continue;
   if(s.getAula().getId().equals(c.aula().getId())) return issue("CHOQUE_AULA","El aula ya está ocupada en esa franja.");
   if(s.getSeccion().getDocente()!=null&&c.seccion().getDocente()!=null&&s.getSeccion().getDocente().getId().equals(c.seccion().getDocente().getId())) return issue("CHOQUE_DOCENTE","El docente ya tiene una sesión en esa franja.");
  } return Optional.empty();
 }
 private Optional<ValidationIssue> issue(String c,String m){return Optional.of(new ValidationIssue(ValidationIssue.Level.ERROR,c,m));}
}
