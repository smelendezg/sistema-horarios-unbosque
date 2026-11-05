package co.edu.unbosque.horarios.api;

import static co.edu.unbosque.horarios.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import co.edu.unbosque.horarios.domain.*;
import co.edu.unbosque.horarios.repository.*;
import co.edu.unbosque.horarios.service.ParameterService;
import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class MissingCasesTest {

  @Test @DisplayName("CP-04: el administrador crea un usuario institucional con clave cifrada")
  void cp04_creaUsuario(){
    var users=mock(UserRepository.class);var encoder=mock(PasswordEncoder.class);
    when(users.findByCorreo("nueva@unbosque.edu.co")).thenReturn(Optional.empty());
    when(encoder.encode("Segura2026!")).thenReturn("bcrypt");
    when(users.save(any(User.class))).thenAnswer(invocation->{var user=invocation.<User>getArgument(0);ReflectionTestUtils.setField(user,"id",9L);return user;});
    var result=new UserController(users,encoder).create(new ApiDtos.UserInput("Nueva Persona","nueva@unbosque.edu.co","Segura2026!","ESTUDIANTE"));
    assertEquals(9L,result.id());assertEquals("ESTUDIANTE",result.rol());
    verify(encoder).encode("Segura2026!");
  }

  @Test @DisplayName("CP-05: el administrador cambia el rol de un usuario")
  void cp05_cambiaRol(){
    var users=mock(UserRepository.class);var user=new User("Persona","persona@unbosque.edu.co","hash",Role.ESTUDIANTE);
    ReflectionTestUtils.setField(user,"id",3L);when(users.findById(3L)).thenReturn(Optional.of(user));when(users.save(user)).thenReturn(user);
    var result=new UserController(users,mock(PasswordEncoder.class)).role(3L,new ApiDtos.RoleInput("DOCENTE"));
    assertEquals("DOCENTE",result.rol());assertEquals(Role.DOCENTE,user.getRol());
  }

  @Test @DisplayName("CP-06: guarda disponibilidad personalizada desde las 16:00")
  @SuppressWarnings("unchecked")
  void cp06_disponibilidadPersonalizada(){
    var teachers=mock(TeacherRepository.class);var jdbc=mock(JdbcTemplate.class);when(teachers.existsById(1L)).thenReturn(true);
    doReturn(List.of(new ApiDtos.AvailabilityView(1L,1,LocalTime.of(16,0),LocalTime.of(20,0))))
        .when(jdbc).query(anyString(),any(RowMapper.class),eq(1L));
    var result=new TeacherAvailabilityController(teachers,jdbc).replace(1L,List.of(new ApiDtos.AvailabilityInput(1,LocalTime.of(16,0),LocalTime.of(20,0))));
    assertEquals(LocalTime.of(16,0),result.getFirst().horaInicio());
    verify(jdbc).update(contains("delete from disponibilidad_docente"),eq(1L));
    verify(jdbc).update(contains("insert into disponibilidad_docente"),eq(1L),eq(1),eq(Time.valueOf("16:00:00")),eq(Time.valueOf("20:00:00")));
  }

  @Test @DisplayName("CP-16 y CP-37: importa matrícula válida y genera sugerencia")
  void cp16_cp37_importaMatricula() throws Exception {
    var repository=mock(ProjectedEnrollmentRepository.class);var periods=mock(AcademicPeriodRepository.class);var courses=mock(CourseRepository.class);var parameters=mock(ParameterService.class);
    var period=period();var course=course();when(periods.findById(1L)).thenReturn(Optional.of(period));when(courses.findByCodigo("SIS-301")).thenReturn(Optional.of(course));when(repository.findByPeriodoIdAndCursoId(1L,1L)).thenReturn(Optional.empty());
    var controller=new EnrollmentController(repository,periods,courses,parameters);
    var file=new MockMultipartFile("file","matricula.csv","text/csv","codigo_curso,demanda\nSIS-301,45\n".getBytes());
    var result=controller.upload(1L,file);
    assertEquals(1,result.correctos());assertTrue(result.errores().isEmpty());verify(repository).save(any(ProjectedEnrollment.class));
  }

  @Test @DisplayName("CP-16: la matrícula importada recomienda dos secciones para demanda 45")
  void cp16_sugerenciaDesdeMatricula(){
    var repository=mock(ProjectedEnrollmentRepository.class);var parameters=mock(ParameterService.class);var enrollment=new ProjectedEnrollment(period(),course(),45);ReflectionTestUtils.setField(enrollment,"id",1L);
    when(repository.findByPeriodoIdOrderByCursoCodigo(1L)).thenReturn(List.of(enrollment));when(parameters.integer("tamano_objetivo",40)).thenReturn(40);when(parameters.integer("tolerancia",10)).thenReturn(10);
    var rows=new EnrollmentController(repository,mock(AcademicPeriodRepository.class),mock(CourseRepository.class),parameters).all(1L);
    assertEquals(2,rows.getFirst().recomendado());
  }

  @Test @DisplayName("CP-38: rechaza CSV con demanda inválida sin guardar filas")
  void cp38_rechazaCsvInvalido() throws Exception {
    var repository=mock(ProjectedEnrollmentRepository.class);var periods=mock(AcademicPeriodRepository.class);when(periods.findById(1L)).thenReturn(Optional.of(period()));
    var controller=new EnrollmentController(repository,periods,mock(CourseRepository.class),mock(ParameterService.class));
    var file=new MockMultipartFile("file","matricula.csv","text/csv","codigo_curso,demanda\nSIS-301,-5\n".getBytes());
    var result=controller.upload(1L,file);
    assertEquals(0,result.correctos());assertEquals(1,result.errores().size());verify(repository,never()).save(any());
  }

  @Test @DisplayName("Disponibilidad: rechaza franjas superpuestas")
  void rechazaDisponibilidadSuperpuesta(){
    var teachers=mock(TeacherRepository.class);when(teachers.existsById(1L)).thenReturn(true);
    var controller=new TeacherAvailabilityController(teachers,mock(JdbcTemplate.class));
    assertThrows(ResponseStatusException.class,()->controller.replace(1L,List.of(
        new ApiDtos.AvailabilityInput(1,LocalTime.of(16,0),LocalTime.of(20,0)),
        new ApiDtos.AvailabilityInput(1,LocalTime.of(18,0),LocalTime.of(21,0)))));
  }
}
