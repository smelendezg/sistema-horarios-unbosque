package co.edu.unbosque.horarios.api;

import co.edu.unbosque.horarios.repository.SystemParameterRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parameters")
public class ParameterController {
  private final SystemParameterRepository parameters;
  public ParameterController(SystemParameterRepository parameters){this.parameters=parameters;}
  @GetMapping @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')") public Object all(){return parameters.findAll();}
  @PutMapping("/{key}") @PreAuthorize("hasAnyRole('DIRECCION','ADMINISTRADOR')")
  public Object update(@PathVariable String key,@Valid @RequestBody ApiDtos.ParameterInput input){var parameter=parameters.findByClave(key).orElseThrow();parameter.setValor(input.valor());return parameters.save(parameter);}
}
