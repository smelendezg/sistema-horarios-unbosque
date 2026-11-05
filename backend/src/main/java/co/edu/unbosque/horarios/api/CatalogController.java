package co.edu.unbosque.horarios.api;
import co.edu.unbosque.horarios.repository.*; import org.springframework.web.bind.annotation.*; import org.springframework.security.access.prepost.PreAuthorize; import java.util.*;
@RestController @RequestMapping("/api/catalogs") @PreAuthorize("hasAnyRole('DIRECCION','COMITE_AREA','COMITE_TACTICO','ADMINISTRADOR')") public class CatalogController {
 private final CourseRepository courses;private final TeacherRepository teachers;private final ClassroomRepository rooms;private final AcademicPeriodRepository periods;
 public CatalogController(CourseRepository c,TeacherRepository t,ClassroomRepository r,AcademicPeriodRepository p){courses=c;teachers=t;rooms=r;periods=p;}
 @GetMapping public Map<String,Object> all(){return Map.of("courses",courses.findAll(),"teachers",teachers.findAll(),"classrooms",rooms.findAll(),"periods",periods.findAll());}
}
