package co.edu.unbosque.horarios.api;
import co.edu.unbosque.horarios.repository.UserRepository; import co.edu.unbosque.horarios.security.JwtService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
 public AuthController(UserRepository u,PasswordEncoder e,JwtService j){users=u;encoder=e;jwt=j;}
 @PostMapping("/login") public ResponseEntity<?> login(@Valid @RequestBody ApiDtos.LoginRequest r){return users.findByCorreo(r.correo()).filter(u->u.isActivo()&&encoder.matches(r.clave(),u.getPasswordHash())).<ResponseEntity<?>>map(u->ResponseEntity.ok(new ApiDtos.LoginResponse(jwt.create(u),u.getNombre(),u.getCorreo(),u.getRol().name()))).orElseGet(()->ResponseEntity.status(401).body(java.util.Map.of("message","Correo o contraseña incorrectos")));}
}
