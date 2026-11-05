package co.edu.unbosque.horarios.api;

import static co.edu.unbosque.horarios.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import co.edu.unbosque.horarios.domain.*;
import co.edu.unbosque.horarios.repository.*;
import co.edu.unbosque.horarios.service.ParameterService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class MasterDataAndSuggestionTest {

  @Test @DisplayName("CP-06: crea docente y registra disponibilidad institucional")
  void cp06_creaDocenteConDisponibilidad(){
    var courses=mock(CourseRepository.class); var teachers=mock(TeacherRepository.class);
    var rooms=mock(ClassroomRepository.class); var sections=mock(SectionRepository.class);
    var periods=mock(AcademicPeriodRepository.class); var jdbc=mock(JdbcTemplate.class);
    when(teachers.save(any(Teacher.class))).thenAnswer(invocation->{
      var teacher=invocation.<Teacher>getArgument(0);
      ReflectionTestUtils.setField(teacher,"id",7L);
      return teacher;
    });
    var controller=new MasterDataController(courses,teachers,rooms,sections,periods,jdbc);

    var saved=controller.createTeacher(new ApiDtos.TeacherInput("D-7","Ana Torres",
        "ana@unbosque.edu.co","CÁTEDRA","ASISTENTE"));

    assertEquals(7L,saved.getId());
    verify(jdbc,times(6)).update(contains("disponibilidad_docente"),
        eq(7L),anyInt(),eq("07:00"),eq("22:00"));
  }

  @Test @DisplayName("CP-07: crea aula con capacidad y computadores")
  void cp07_creaAula(){
    var rooms=mock(ClassroomRepository.class);
    when(rooms.save(any(Classroom.class))).thenAnswer(i->i.getArgument(0));
    var controller=masterController(mock(CourseRepository.class),mock(TeacherRepository.class),rooms);
    var saved=controller.createRoom(new ApiDtos.ClassroomInput("LAB-7","Laboratorio 7",45,true,false));
    assertEquals(45,saved.getCapacidad());
    assertTrue(saved.isComputadores());
  }

  @Test @DisplayName("CP-08: actualiza los créditos de un curso")
  void cp08_actualizaCurso(){
    var courses=mock(CourseRepository.class);
    var existing=course();
    when(courses.findById(1L)).thenReturn(Optional.of(existing));
    when(courses.save(existing)).thenReturn(existing);
    var controller=masterController(courses,mock(TeacherRepository.class),mock(ClassroomRepository.class));
    var saved=controller.updateCourse(1L,new ApiDtos.CourseInput("SIS-301","Bases de Datos",4,2,false,false));
    assertEquals(4,saved.getCreditos());
  }

  @Test @DisplayName("CP-10: actualiza el parámetro de tamaño objetivo")
  void cp10_actualizaParametro() throws Exception {
    var repository=mock(SystemParameterRepository.class);
    var constructor=SystemParameter.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    var parameter=constructor.newInstance();
    ReflectionTestUtils.setField(parameter,"clave","tamano_objetivo");
    ReflectionTestUtils.setField(parameter,"valor","40");
    when(repository.findByClave("tamano_objetivo")).thenReturn(Optional.of(parameter));
    when(repository.save(parameter)).thenReturn(parameter);
    var saved=(SystemParameter)new ParameterController(repository)
        .update("tamano_objetivo",new ApiDtos.ParameterInput("35"));
    assertEquals("35",saved.getValor());
  }

  @Test @DisplayName("CP-12: demanda 70 y tamaño 35 recomienda dos secciones")
  void cp12_sugiereDosSecciones(){
    assertSuggestion(35,0,70,2);
  }

  @Test @DisplayName("CP-13: demanda 40 con objetivo 40 y tolerancia 10 recomienda una sección")
  void cp13_limiteObjetivo(){
    assertSuggestion(40,10,40,1);
  }

  @Test @DisplayName("CP-14: demanda 44 usa una sección por la tolerancia del diez por ciento")
  void cp14_limiteTolerancia(){
    assertSuggestion(40,10,44,1);
  }

  @Test @DisplayName("CP-15: demanda 45 supera la tolerancia y recomienda dos secciones")
  void cp15_superaTolerancia(){
    assertSuggestion(40,10,45,2);
  }

  private void assertSuggestion(int target,int tolerance,int demand,int expected){
    var parameters=mock(ParameterService.class);
    when(parameters.integer("tamano_objetivo",40)).thenReturn(target);
    when(parameters.integer("tolerancia",10)).thenReturn(tolerance);
    var controller=new SectionController(mock(SectionRepository.class),mock(CourseRepository.class),
        mock(AcademicPeriodRepository.class),mock(TeacherRepository.class),parameters);
    var result=controller.suggest(new ApiDtos.SuggestionRequest(1L,1L,demand,1L));
    assertEquals(expected,result.recomendado());
    assertEquals(expected,result.minimo());
  }

  private MasterDataController masterController(CourseRepository courses,TeacherRepository teachers,
                                                 ClassroomRepository rooms){
    return new MasterDataController(courses,teachers,rooms,mock(SectionRepository.class),
        mock(AcademicPeriodRepository.class),mock(JdbcTemplate.class));
  }
}
