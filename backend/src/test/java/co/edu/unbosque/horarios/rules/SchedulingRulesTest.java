package co.edu.unbosque.horarios.rules;

import static co.edu.unbosque.horarios.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class SchedulingRulesTest {

  @Test @DisplayName("CP-17: mueve una sesión conservando su duración")
  void cp17_mueveSesion(){
    var schedule=schedule(co.edu.unbosque.horarios.domain.ScheduleStatus.BORRADOR);
    var session=session(schedule,section(),room(),1,LocalTime.of(7,0),10L);
    var destination=withId(new co.edu.unbosque.horarios.domain.Classroom("B-201","Aula",45,true,true),2L);
    session.move(destination,3,LocalTime.of(13,0),2);
    assertEquals(3,session.getDiaSemana());
    assertEquals(LocalTime.of(13,0),session.getHoraInicio());
    assertEquals(LocalTime.of(15,0),session.getHoraFin());
    assertEquals(2L,session.getAula().getId());
  }

  @Test @DisplayName("CP-22: detecta choque de docente en la misma franja")
  void cp22_detectaChoqueDocente(){
    var teacher=teacher();
    var first=section(course(),teacher,30,1L);
    var second=section(course(),teacher,30,2L);
    var schedule=schedule(co.edu.unbosque.horarios.domain.ScheduleStatus.BORRADOR);
    var existing=session(schedule,first,room(),2,LocalTime.of(9,0),10L);
    var otherRoom=withId(new co.edu.unbosque.horarios.domain.Classroom("B-201","Aula",45,true,true),2L);
    var candidate=new AssignmentCandidate(schedule,second,otherRoom,2,LocalTime.of(9,0),2,List.of(existing),null);
    var issue=new CollisionRule().validate(candidate);
    assertTrue(issue.isPresent());
    assertEquals("CHOQUE_DOCENTE",issue.get().codigo());
  }

  @Test @DisplayName("CP-23: rechaza un aula con capacidad insuficiente")
  void cp23_rechazaCapacidadInsuficiente(){
    var candidate=new AssignmentCandidate(null,section(course(),teacher(),44,1L),room(30,true,true),2,LocalTime.of(9,0),2,List.of(),null);
    var issue=new ClassroomCapacityRule().validate(candidate);
    assertTrue(issue.isPresent());
    assertEquals("CAPACIDAD",issue.get().codigo());
  }

  @Test @DisplayName("CP-24: rechaza una franja fuera de la disponibilidad del docente")
  void cp24_rechazaFueraDeDisponibilidad(){
    var jdbc=mock(JdbcTemplate.class);
    when(jdbc.queryForObject(contains("docente_curso"),eq(Integer.class),any(),any())).thenReturn(1);
    when(jdbc.queryForObject(contains("disponibilidad_docente"),eq(Integer.class),any(),any(),any(),any())).thenReturn(0);
    var issue=new TeacherEligibilityRule(jdbc).validate(new AssignmentCandidate(null,section(),room(),2,LocalTime.of(14,0),2,List.of(),null));
    assertTrue(issue.isPresent());
    assertEquals("DISPONIBILIDAD",issue.get().codigo());
  }

  @Test @DisplayName("CP-25: rechaza un docente no habilitado para el curso")
  void cp25_rechazaDocenteNoHabilitado(){
    var jdbc=mock(JdbcTemplate.class);
    when(jdbc.queryForObject(contains("docente_curso"),eq(Integer.class),any(),any())).thenReturn(0);
    var issue=new TeacherEligibilityRule(jdbc).validate(new AssignmentCandidate(null,section(),room(),2,LocalTime.of(14,0),2,List.of(),null));
    assertTrue(issue.isPresent());
    assertEquals("DOCENTE_CURSO",issue.get().codigo());
  }

  @Test @DisplayName("CP-27: advierte sesiones del mismo curso en días consecutivos")
  void cp27_advierteDiasConsecutivos(){
    var section=section(course(2,false,false),teacher(),30,1L);
    var existing=session(schedule(co.edu.unbosque.horarios.domain.ScheduleStatus.BORRADOR),section,room(),1,LocalTime.of(9,0),10L);
    var issue=new ConsecutiveDaysRule().validate(new AssignmentCandidate(null,section,room(),2,LocalTime.of(9,0),2,List.of(existing),null));
    assertTrue(issue.isPresent());
    assertEquals(ValidationIssue.Level.ADVERTENCIA,issue.get().nivel());
  }

  @Test @DisplayName("CP-26: conserva todos los conflictos detectados como errores")
  void cp26_reportaConflictosSimultaneos(){
    var teacher=teacher();
    var first=section(course(),teacher,30,1L);
    var second=section(course(),teacher,44,2L);
    var schedule=schedule(co.edu.unbosque.horarios.domain.ScheduleStatus.BORRADOR);
    var existing=session(schedule,first,room(),2,LocalTime.of(9,0),10L);
    var engine=new RulesEngine(List.of(new CollisionRule(),new ClassroomCapacityRule()));
    var issues=engine.validate(new AssignmentCandidate(schedule,second,room(30,true,true),
        2,LocalTime.of(9,0),2,List.of(existing),null));
    assertEquals(2,issues.size());
    assertTrue(issues.stream().allMatch(issue->issue.nivel()==ValidationIssue.Level.ERROR));
  }

  @Test @DisplayName("Validación adicional: exige computadores cuando el curso los requiere")
  void rechazaAulaSinComputadores(){
    var candidate=new AssignmentCandidate(null,section(course(2,true,false),teacher(),30,1L),room(45,false,true),2,LocalTime.of(9,0),2,List.of(),null);
    assertEquals("EQUIPO",new ClassroomCapacityRule().validate(candidate).orElseThrow().codigo());
  }

  @Test @DisplayName("CP-19: sesenta recálculos del motor tardan menos de dos segundos")
  void cp19_recalculoMenorADosSegundos(){
    var engine=new RulesEngine(List.of(new ClassroomCapacityRule(),new ConsecutiveDaysRule()));
    var candidate=new AssignmentCandidate(null,section(),room(),3,LocalTime.of(14,0),2,List.of(),null);
    assertTimeout(Duration.ofSeconds(2),()->{
      for(int i=0;i<60;i++)engine.validate(candidate);
    });
  }
}
