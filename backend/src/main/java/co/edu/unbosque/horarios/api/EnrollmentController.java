package co.edu.unbosque.horarios.api;

import co.edu.unbosque.horarios.domain.ProjectedEnrollment;
import co.edu.unbosque.horarios.repository.*;
import co.edu.unbosque.horarios.service.ParameterService;
import jakarta.transaction.Transactional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/enrollments")
@PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR','COMITE_AREA')")
public class EnrollmentController {
  private final ProjectedEnrollmentRepository enrollments; private final AcademicPeriodRepository periods;
  private final CourseRepository courses; private final ParameterService parameters;
  public EnrollmentController(ProjectedEnrollmentRepository enrollments,AcademicPeriodRepository periods,CourseRepository courses,ParameterService parameters){this.enrollments=enrollments;this.periods=periods;this.courses=courses;this.parameters=parameters;}
  @GetMapping public List<ApiDtos.EnrollmentView> all(@RequestParam Long periodId){return enrollments.findByPeriodoIdOrderByCursoCodigo(periodId).stream().map(this::view).toList();}
  @PostMapping(value="/import",consumes="multipart/form-data") @Transactional
  public ApiDtos.ImportResult upload(@RequestParam Long periodId,@RequestPart("file") MultipartFile file) throws IOException {
    var period=periods.findById(periodId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Periodo no encontrado."));
    if(file.isEmpty()||file.getOriginalFilename()==null||!file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".csv"))throw bad("Selecciona un archivo CSV válido.");
    var rows=new ArrayList<Row>();var errors=new ArrayList<String>();
    try(var reader=new BufferedReader(new InputStreamReader(file.getInputStream(),StandardCharsets.UTF_8))){
      String header=reader.readLine();
      if(header==null||!normalize(header).equals("codigo_curso,demanda"))throw bad("El CSV debe contener las columnas codigo_curso,demanda.");
      String line;int number=1;
      while((line=reader.readLine())!=null){number++;if(line.isBlank())continue;var parts=line.split(",",-1);
        if(parts.length!=2){errors.add("Fila "+number+": debe contener código y demanda.");continue;}
        String code=parts[0].trim();int demand;
        try{demand=Integer.parseInt(parts[1].trim());if(demand<0)throw new NumberFormatException();}catch(NumberFormatException e){errors.add("Fila "+number+": demanda inválida.");continue;}
        var course=courses.findByCodigo(code).orElse(null);if(course==null){errors.add("Fila "+number+": el curso "+code+" no existe.");continue;}
        rows.add(new Row(course,demand));
      }
    }
    if(!errors.isEmpty())return new ApiDtos.ImportResult(rows.size()+errors.size(),0,errors);
    for(var row:rows){var current=enrollments.findByPeriodoIdAndCursoId(periodId,row.course().getId()).orElse(null);if(current==null)enrollments.save(new ProjectedEnrollment(period,row.course(),row.demand()));else{current.update(row.demand());enrollments.save(current);}}
    return new ApiDtos.ImportResult(rows.size(),rows.size(),List.of());
  }
  private ApiDtos.EnrollmentView view(ProjectedEnrollment item){int target=parameters.integer("tamano_objetivo",40),tolerance=parameters.integer("tolerancia",10);int max=(int)Math.ceil(target*(1+tolerance/100.0));int count=Math.max(1,(int)Math.ceil(item.getDemanda()/(double)max));return new ApiDtos.EnrollmentView(item.getId(),item.getPeriodo().getId(),item.getPeriodo().getCodigo(),item.getCurso().getId(),item.getCurso().getCodigo(),item.getCurso().getNombre(),item.getDemanda(),count,count,item.getFechaImportacion());}
  private String normalize(String value){return value.replace("\uFEFF","").trim().toLowerCase(Locale.ROOT).replace(" ","");}
  private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
  private record Row(co.edu.unbosque.horarios.domain.Course course,int demand){}
}
