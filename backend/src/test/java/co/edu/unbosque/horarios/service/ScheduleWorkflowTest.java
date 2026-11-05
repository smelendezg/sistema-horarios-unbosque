package co.edu.unbosque.horarios.service;

import static co.edu.unbosque.horarios.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import co.edu.unbosque.horarios.api.ApiDtos;
import co.edu.unbosque.horarios.domain.*;
import co.edu.unbosque.horarios.repository.*;
import co.edu.unbosque.horarios.rules.RulesEngine;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class ScheduleWorkflowTest {
  private ScheduleRepository schedules;
  private SectionRepository sections;
  private ClassroomRepository rooms;
  private ClassSessionRepository sessions;
  private RulesEngine rules;
  private UserRepository users;
  private ScheduleReviewRepository reviews;
  private JdbcTemplate jdbc;
  private ParameterService parameters;
  private ScheduleService service;

  @BeforeEach void setUp(){
    schedules=mock(ScheduleRepository.class); sections=mock(SectionRepository.class);
    rooms=mock(ClassroomRepository.class); sessions=mock(ClassSessionRepository.class);
    rules=mock(RulesEngine.class); users=mock(UserRepository.class);
    reviews=mock(ScheduleReviewRepository.class); jdbc=mock(JdbcTemplate.class);
    parameters=mock(ParameterService.class);
    service=new ScheduleService(schedules,sections,rooms,sessions,rules,users,reviews,jdbc,parameters);
    when(sections.findByPeriodoId(1L)).thenReturn(List.of());
    when(sessions.findByHorarioId(anyLong())).thenReturn(List.of());
  }

  @Test @DisplayName("CP-28: envía un borrador completo a revisión")
  void cp28_enviaARevision(){
    var schedule=givenSchedule(ScheduleStatus.BORRADOR);
    var result=service.transition(1L,ScheduleStatus.EN_REVISION);
    assertEquals("EN_REVISION",result.status());
    assertEquals(ScheduleStatus.EN_REVISION,schedule.getEstado());
  }

  @Test @DisplayName("CP-29: impide aprobar directamente un borrador")
  void cp29_transicionInvalida(){
    givenSchedule(ScheduleStatus.BORRADOR);
    var error=assertThrows(ResponseStatusException.class,
        ()->service.transition(1L,ScheduleStatus.APROBADO));
    assertEquals(HttpStatus.CONFLICT,error.getStatusCode());
  }

  @Test @DisplayName("CP-30: Comité Táctico aprueba un horario en revisión")
  void cp30_apruebaRevision(){
    givenSchedule(ScheduleStatus.EN_REVISION);
    var reviewer=new User("Tomás Táctico","tactico@unbosque.edu.co","hash",Role.COMITE_TACTICO);
    when(users.findByCorreo("tactico@unbosque.edu.co")).thenReturn(Optional.of(reviewer));
    var result=service.review(1L,new ApiDtos.ReviewRequest("APROBADO","Sin observaciones"),
        "tactico@unbosque.edu.co");
    assertEquals("APROBADO",result.status());
    verify(reviews).save(any(ScheduleReview.class));
  }

  @Test @DisplayName("CP-31: devuelve a borrador y conserva las observaciones")
  void cp31_devuelveConObservaciones(){
    givenSchedule(ScheduleStatus.EN_REVISION);
    var reviewer=new User("Tomás Táctico","tactico@unbosque.edu.co","hash",Role.COMITE_TACTICO);
    when(users.findByCorreo("tactico@unbosque.edu.co")).thenReturn(Optional.of(reviewer));
    when(reviews.save(any(ScheduleReview.class))).thenAnswer(i->i.getArgument(0));
    var result=service.review(1L,new ApiDtos.ReviewRequest("DEVUELTO","Cambiar el aula"),
        "tactico@unbosque.edu.co");
    assertEquals("BORRADOR",result.status());
    verify(reviews).save(argThat(review->"DEVUELTO".equals(review.getDecision())
        && "Cambiar el aula".equals(review.getObservaciones())));
  }

  @Test @DisplayName("CP-32: Dirección publica un horario aprobado")
  void cp32_publicaAprobado(){
    givenSchedule(ScheduleStatus.APROBADO);
    assertEquals("PUBLICADO",service.transition(1L,ScheduleStatus.PUBLICADO).status());
  }

  @Test @DisplayName("CP-33: no permite publicar un borrador")
  void cp33_noPublicaBorrador(){
    givenSchedule(ScheduleStatus.BORRADOR);
    assertThrows(ResponseStatusException.class,
        ()->service.transition(1L,ScheduleStatus.PUBLICADO));
  }

  @Test @DisplayName("CP-34: un horario publicado queda bloqueado para edición")
  void cp34_bloqueaEdicion(){
    givenSchedule(ScheduleStatus.PUBLICADO);
    var error=assertThrows(ResponseStatusException.class,()->service.deleteSession(1L,99L));
    assertEquals(HttpStatus.CONFLICT,error.getStatusCode());
    verify(sessions,never()).delete(any());
  }

  @Test @DisplayName("CP-35: la consulta pública entrega la última versión publicada")
  void cp35_consultaPublicada(){
    var published=givenSchedule(ScheduleStatus.PUBLICADO);
    when(schedules.findFirstByEstadoOrderByIdDesc(ScheduleStatus.PUBLICADO))
        .thenReturn(Optional.of(published));
    assertEquals("PUBLICADO",service.published().status());
  }

  @Test @DisplayName("CP-36: un borrador nunca aparece en la consulta pública")
  void cp36_borradorNoEsPublico(){
    when(schedules.findFirstByEstadoOrderByIdDesc(ScheduleStatus.PUBLICADO))
        .thenReturn(Optional.empty());
    var error=assertThrows(ResponseStatusException.class,service::published);
    assertEquals(HttpStatus.NOT_FOUND,error.getStatusCode());
  }

  @Test @DisplayName("Regresión: solo crea una nueva versión después de publicar la actual")
  void creaNuevaVersionDespuesDePublicar(){
    var current=givenSchedule(ScheduleStatus.PUBLICADO);
    when(schedules.findAll()).thenReturn(List.of(current));
    // El repositorio simulado debe devolver la versión recién guardada al consultar su detalle.
    final Schedule[] created=new Schedule[1];
    when(schedules.save(any(Schedule.class))).thenAnswer(invocation->{
      created[0]=invocation.getArgument(0);
      ReflectionTestUtils.setField(created[0],"id",2L);
      return created[0];
    });
    when(schedules.findById(2L)).thenAnswer(invocation->Optional.ofNullable(created[0]));

    var result=service.createNextVersion();
    assertEquals(2,result.version());
    assertEquals("BORRADOR",result.status());
  }

  private Schedule givenSchedule(ScheduleStatus status){
    var schedule=schedule(status);
    when(schedules.findById(1L)).thenReturn(Optional.of(schedule));
    return schedule;
  }
}
