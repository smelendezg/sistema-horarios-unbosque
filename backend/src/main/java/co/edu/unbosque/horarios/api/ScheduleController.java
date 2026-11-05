package co.edu.unbosque.horarios.api;

import co.edu.unbosque.horarios.domain.ScheduleStatus;
import co.edu.unbosque.horarios.repository.ScheduleReviewRepository;
import co.edu.unbosque.horarios.service.ScheduleService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
  private final ScheduleService service;
  private final ScheduleReviewRepository reviews;
  public ScheduleController(ScheduleService service, ScheduleReviewRepository reviews){this.service=service;this.reviews=reviews;}

  @GetMapping("/current") @PreAuthorize("hasAnyRole('DIRECCION','COMITE_AREA','COMITE_TACTICO','ADMINISTRADOR')") public ApiDtos.ScheduleView current(){return service.current();}
  @PostMapping @PreAuthorize("hasAnyRole('DIRECCION','COMITE_AREA')") public ApiDtos.ScheduleView create(){return service.createNextVersion();}
  @GetMapping("/{id}") @PreAuthorize("hasAnyRole('DIRECCION','COMITE_AREA','COMITE_TACTICO','ADMINISTRADOR')") public ApiDtos.ScheduleView get(@PathVariable Long id){return service.get(id);}
  @GetMapping("/{id}/reviews") @PreAuthorize("hasAnyRole('DIRECCION','COMITE_AREA','COMITE_TACTICO')") public Object history(@PathVariable Long id){return reviews.findByHorarioIdOrderByFechaDesc(id).stream().map(r->new ApiDtos.ReviewView(r.getId(),r.getDecision(),r.getObservaciones(),r.getFecha(),r.getUsuario().getNombre())).toList();}
  @PostMapping("/{id}/sessions") @PreAuthorize("hasAnyRole('COMITE_AREA','DIRECCION')")
  public ApiDtos.AssignmentResult assign(@PathVariable Long id,@Valid @RequestBody ApiDtos.AssignRequest request){return service.assign(id,request);}
  @PutMapping("/{id}/sessions/{sessionId}") @PreAuthorize("hasAnyRole('COMITE_AREA','DIRECCION')")
  public ApiDtos.AssignmentResult move(@PathVariable Long id,@PathVariable Long sessionId,@Valid @RequestBody ApiDtos.MoveSessionRequest request){return service.move(id,sessionId,request);}
  @DeleteMapping("/{id}/sessions/{sessionId}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('COMITE_AREA','DIRECCION')")
  public void delete(@PathVariable Long id,@PathVariable Long sessionId){service.deleteSession(id,sessionId);}
  @PostMapping("/{id}/submit") @PreAuthorize("hasAnyRole('COMITE_AREA','DIRECCION')")
  public ApiDtos.ScheduleView submit(@PathVariable Long id){return service.transition(id,ScheduleStatus.EN_REVISION);}
  @PostMapping("/{id}/review") @PreAuthorize("hasRole('COMITE_TACTICO')")
  public ApiDtos.ScheduleView review(@PathVariable Long id,@Valid @RequestBody ApiDtos.ReviewRequest request,Principal principal){return service.review(id,request,principal.getName());}
  @PostMapping("/{id}/publish") @PreAuthorize("hasRole('DIRECCION')")
  public ApiDtos.ScheduleView publish(@PathVariable Long id){return service.transition(id,ScheduleStatus.PUBLICADO);}
}
