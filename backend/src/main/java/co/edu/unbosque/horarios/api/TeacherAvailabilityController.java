package co.edu.unbosque.horarios.api;

import co.edu.unbosque.horarios.repository.TeacherRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.sql.Time;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/teachers/{teacherId}/availability")
public class TeacherAvailabilityController {
  private final TeacherRepository teachers; private final JdbcTemplate jdbc;
  public TeacherAvailabilityController(TeacherRepository teachers,JdbcTemplate jdbc){this.teachers=teachers;this.jdbc=jdbc;}
  @GetMapping @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR','COMITE_AREA','COMITE_TACTICO')")
  public List<ApiDtos.AvailabilityView> all(@PathVariable Long teacherId){requireTeacher(teacherId);return jdbc.query("select id,dia_semana,hora_inicio,hora_fin from disponibilidad_docente where id_docente=? order by dia_semana,hora_inicio",(rs,n)->new ApiDtos.AvailabilityView(rs.getLong("id"),rs.getInt("dia_semana"),rs.getTime("hora_inicio").toLocalTime(),rs.getTime("hora_fin").toLocalTime()),teacherId);}
  @PutMapping @Transactional @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')")
  public List<ApiDtos.AvailabilityView> replace(@PathVariable Long teacherId,@Valid @RequestBody List<ApiDtos.AvailabilityInput> ranges){
    requireTeacher(teacherId);validate(ranges);jdbc.update("delete from disponibilidad_docente where id_docente=?",teacherId);
    for(var range:ranges)jdbc.update("insert into disponibilidad_docente(id_docente,dia_semana,hora_inicio,hora_fin) values (?,?,?,?)",teacherId,range.dia(),Time.valueOf(range.horaInicio()),Time.valueOf(range.horaFin()));
    return all(teacherId);
  }
  private void validate(List<ApiDtos.AvailabilityInput> ranges){
    if(ranges.isEmpty())throw bad("Agrega al menos una franja de disponibilidad.");
    for(var range:ranges)if(!range.horaFin().isAfter(range.horaInicio()))throw bad("La hora final debe ser posterior a la inicial.");
    for(int i=0;i<ranges.size();i++)for(int j=i+1;j<ranges.size();j++){
      var a=ranges.get(i);var b=ranges.get(j);
      if(a.dia()==b.dia()&&a.horaInicio().isBefore(b.horaFin())&&b.horaInicio().isBefore(a.horaFin()))throw bad("Hay franjas de disponibilidad superpuestas.");
    }
  }
  private void requireTeacher(Long id){if(!teachers.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Docente no encontrado.");}
  private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
}
