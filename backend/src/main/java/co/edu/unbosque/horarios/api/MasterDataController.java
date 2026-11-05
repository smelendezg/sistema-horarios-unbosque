package co.edu.unbosque.horarios.api;

import co.edu.unbosque.horarios.domain.*;
import co.edu.unbosque.horarios.repository.*;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/master-data")
public class MasterDataController {
  private final CourseRepository courses; private final TeacherRepository teachers;
  private final ClassroomRepository rooms; private final SectionRepository sections;
  private final AcademicPeriodRepository periods;
  private final JdbcTemplate jdbc;
  public MasterDataController(CourseRepository c,TeacherRepository t,ClassroomRepository r,SectionRepository s,AcademicPeriodRepository p,JdbcTemplate jdbc){courses=c;teachers=t;rooms=r;sections=s;periods=p;this.jdbc=jdbc;}
  @GetMapping @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR','COMITE_AREA','COMITE_TACTICO')") public Object all(){return Map.of("courses",courses.findAll(),"teachers",teachers.findAll(),"classrooms",rooms.findAll(),"sections",sections.findAll(),"periods",periods.findAll());}
  @PostMapping("/courses") @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')") public Course createCourse(@Valid @RequestBody ApiDtos.CourseInput x){return courses.save(new Course(x.codigo(),x.nombre(),x.creditos(),x.frecuenciaSemanal(),x.requiereComputadores(),x.requiereSillasMoviles()));}
  @PutMapping("/courses/{id}") @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')") public Course updateCourse(@PathVariable Long id,@Valid @RequestBody ApiDtos.CourseInput x){var e=courses.findById(id).orElseThrow();e.update(x.codigo(),x.nombre(),x.creditos(),x.frecuenciaSemanal(),x.requiereComputadores(),x.requiereSillasMoviles());return courses.save(e);}
  @PostMapping("/teachers") @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')") public Teacher createTeacher(@Valid @RequestBody ApiDtos.TeacherInput x){var teacher=teachers.save(new Teacher(x.idExterno(),x.nombre(),x.correo(),x.vinculacion(),x.escalafon()));for(int day=1;day<=6;day++)jdbc.update("insert into disponibilidad_docente(id_docente,dia_semana,hora_inicio,hora_fin) values (?,?,cast(? as time),cast(? as time))",teacher.getId(),day,"07:00","22:00");return teacher;}
  @PutMapping("/teachers/{id}") @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')") public Teacher updateTeacher(@PathVariable Long id,@Valid @RequestBody ApiDtos.TeacherInput x){var e=teachers.findById(id).orElseThrow();e.update(x.idExterno(),x.nombre(),x.correo(),x.vinculacion(),x.escalafon());return teachers.save(e);}
  @PostMapping("/classrooms") @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')") public Classroom createRoom(@Valid @RequestBody ApiDtos.ClassroomInput x){return rooms.save(new Classroom(x.codigo(),x.nombre(),x.capacidad(),x.computadores(),x.sillasMoviles()));}
  @PutMapping("/classrooms/{id}") @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')") public Classroom updateRoom(@PathVariable Long id,@Valid @RequestBody ApiDtos.ClassroomInput x){var e=rooms.findById(id).orElseThrow();e.update(x.codigo(),x.nombre(),x.capacidad(),x.computadores(),x.sillasMoviles());return rooms.save(e);}
  @PostMapping("/sections") @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')") public Section createSection(@Valid @RequestBody ApiDtos.SectionInput x){qualify(x.docenteId(),x.cursoId());return sections.save(new Section(course(x.cursoId()),period(x.periodoId()),teacher(x.docenteId()),x.numero(),x.estudiantesEstimados()));}
  @PutMapping("/sections/{id}") @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')") public Section updateSection(@PathVariable Long id,@Valid @RequestBody ApiDtos.SectionInput x){qualify(x.docenteId(),x.cursoId());var e=sections.findById(id).orElseThrow();e.update(course(x.cursoId()),period(x.periodoId()),teacher(x.docenteId()),x.numero(),x.estudiantesEstimados());return sections.save(e);}
  @DeleteMapping("/{type}/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')")
  public void delete(@PathVariable String type,@PathVariable Long id){try{switch(type){case "courses"->courses.deleteById(id);case "teachers"->teachers.deleteById(id);case "classrooms"->rooms.deleteById(id);case "sections"->sections.deleteById(id);default->throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tipo no válido.");}}catch(ResponseStatusException e){throw e;}catch(Exception e){throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede eliminar porque el registro está siendo utilizado.");}}
  private Course course(Long id){return courses.findById(id).orElseThrow();}
  private Teacher teacher(Long id){return teachers.findById(id).orElseThrow();}
  private AcademicPeriod period(Long id){return periods.findById(id).orElseThrow();}
  private void qualify(Long teacherId,Long courseId){jdbc.update("insert into docente_curso(id_docente,id_curso) values (?,?) on conflict do nothing",teacherId,courseId);}
}
