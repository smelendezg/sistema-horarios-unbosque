package co.edu.unbosque.horarios.api;
import co.edu.unbosque.horarios.service.ScheduleService; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/public") public class PublicController {private final ScheduleService service;public PublicController(ScheduleService s){service=s;}@GetMapping("/schedule") public ApiDtos.ScheduleView published(){return service.published();}}
