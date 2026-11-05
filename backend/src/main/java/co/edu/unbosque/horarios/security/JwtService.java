package co.edu.unbosque.horarios.security;
import co.edu.unbosque.horarios.domain.User; import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.*; import java.util.Date;
@Service public class JwtService {
 private final SecretKey key; public JwtService(@Value("${app.jwt-secret}") String secret){key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));}
 public String create(User u){return Jwts.builder().subject(u.getCorreo()).claim("role",u.getRol().name()).claim("name",u.getNombre()).issuedAt(new Date()).expiration(Date.from(Instant.now().plus(Duration.ofHours(8)))).signWith(key).compact();}
 public String subject(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
}
