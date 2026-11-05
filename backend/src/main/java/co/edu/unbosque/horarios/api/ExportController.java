package co.edu.unbosque.horarios.api;

import co.edu.unbosque.horarios.service.ExportService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules/{id}")
@PreAuthorize("hasAnyRole('DIRECCION','COMITE_AREA')")
public class ExportController {
  private final ExportService service;
  public ExportController(ExportService service){this.service=service;}
  @GetMapping("/export.pdf") public ResponseEntity<byte[]> pdf(@PathVariable Long id){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=horario-unbosque.pdf").contentType(MediaType.APPLICATION_PDF).body(service.pdf(id));}
  @GetMapping("/export.xlsx") public ResponseEntity<byte[]> xlsx(@PathVariable Long id){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=horario-unbosque.xlsx").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(service.xlsx(id));}
}
