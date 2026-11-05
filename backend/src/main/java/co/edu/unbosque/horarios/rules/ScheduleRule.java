package co.edu.unbosque.horarios.rules;
import java.util.Optional;
public interface ScheduleRule { Optional<ValidationIssue> validate(AssignmentCandidate candidate); }
