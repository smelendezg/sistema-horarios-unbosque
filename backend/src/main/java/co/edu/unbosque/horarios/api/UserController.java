package co.edu.unbosque.horarios.api;

import co.edu.unbosque.horarios.domain.*;
import co.edu.unbosque.horarios.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/users") @PreAuthorize("hasRole('ADMINISTRADOR')")
public class UserController {
  private final UserRepository users; private final PasswordEncoder encoder;
  public UserController(UserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
  @GetMapping public Object all(){return users.findAll().stream().map(this::view).toList();}
  @PostMapping @ResponseStatus(HttpStatus.CREATED)
  public ApiDtos.UserView create(@Valid @RequestBody ApiDtos.UserInput input){
    String email=input.correo().trim().toLowerCase(Locale.ROOT);
    if(users.findByCorreo(email).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"El correo ya está registrado.");
    var saved=users.save(new User(input.nombre().trim(),email,encoder.encode(input.clave()),role(input.rol())));
    return view(saved);
  }
  @PutMapping("/{id}/role") public ApiDtos.UserView role(@PathVariable Long id,@Valid @RequestBody ApiDtos.RoleInput input){
    var user=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado."));
    user.setRol(role(input.rol()));return view(users.save(user));
  }
  @PutMapping("/{id}/status") public ApiDtos.UserView status(@PathVariable Long id,@RequestBody ApiDtos.StatusInput input){
    var user=users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado."));
    user.setActivo(input.activo());return view(users.save(user));
  }
  private Role role(String value){try{return Role.valueOf(value.toUpperCase(Locale.ROOT));}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Rol no válido.");}}
  private ApiDtos.UserView view(User u){return new ApiDtos.UserView(u.getId(),u.getNombre(),u.getCorreo(),u.getRol().name(),u.isActivo());}
}
