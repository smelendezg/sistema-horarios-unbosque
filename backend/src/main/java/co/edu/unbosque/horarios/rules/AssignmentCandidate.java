package co.edu.unbosque.horarios.rules;
import co.edu.unbosque.horarios.domain.*; import java.time.LocalTime; import java.util.List;
public record AssignmentCandidate(Schedule horario,Section seccion,Classroom aula,int dia,LocalTime inicio,int duracionHoras,List<ClassSession> existentes,Long sesionEditada){
 public LocalTime fin(){return inicio.plusHours(duracionHoras);} public boolean overlaps(ClassSession s){return inicio.isBefore(s.getHoraFin())&&fin().isAfter(s.getHoraInicio());}
}
