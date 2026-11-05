package co.edu.unbosque.horarios.rules;
import org.springframework.stereotype.Service; import java.util.List;
@Service public class RulesEngine {
 private final List<ScheduleRule> rules; public RulesEngine(List<ScheduleRule> rules){this.rules=rules;}
 public List<ValidationIssue> validate(AssignmentCandidate c){return rules.stream().map(r->r.validate(c)).flatMap(java.util.Optional::stream).toList();}
}
