package co.edu.unbosque.horarios;

import co.edu.unbosque.horarios.domain.*;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.test.util.ReflectionTestUtils;

public final class TestFixtures {
  private TestFixtures() {}

  public static <T> T withId(T entity,long id){
    ReflectionTestUtils.setField(entity,"id",id);
    return entity;
  }

  public static AcademicPeriod period(){
    return withId(new AcademicPeriod("2026-2",LocalDate.of(2026,7,27),LocalDate.of(2026,11,28)),1L);
  }

  public static Course course(){return course(2,false,false);}
  public static Course course(int frequency,boolean computers,boolean movableChairs){
    return withId(new Course("SIS-301","Bases de Datos",3,frequency,computers,movableChairs),1L);
  }

  public static Teacher teacher(){
    return withId(new Teacher("TH-1","Laura Méndez","laura@unbosque.edu.co","TIEMPO_COMPLETO","ASISTENTE"),1L);
  }

  public static Classroom room(){return room(45,true,true);}
  public static Classroom room(int capacity,boolean computers,boolean movableChairs){
    return withId(new Classroom("LAB-3","Laboratorio",capacity,computers,movableChairs),1L);
  }

  public static Section section(){return section(course(),teacher(),40,1L);}
  public static Section section(Course course,Teacher teacher,int students,long id){
    return withId(new Section(course,period(),teacher,1,students),id);
  }

  public static Schedule schedule(ScheduleStatus status){
    var schedule=withId(new Schedule(period()),1L);
    schedule.setEstado(status);
    return schedule;
  }

  public static ClassSession session(Schedule schedule,Section section,Classroom room,int day,LocalTime start,long id){
    return withId(new ClassSession(schedule,section,room,day,start,2),id);
  }
}
