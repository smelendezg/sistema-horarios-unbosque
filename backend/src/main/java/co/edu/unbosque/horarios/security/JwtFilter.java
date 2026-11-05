package co.edu.unbosque.horarios.security;
import co.edu.unbosque.horarios.repository.UserRepository; import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.util.List;
@Component public class JwtFilter extends OncePerRequestFilter {
 private final JwtService jwt; private final UserRepository users; public JwtFilter(JwtService j,UserRepository u){jwt=j;users=u;}
 protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  var h=req.getHeader("Authorization"); if(h!=null&&h.startsWith("Bearer "))try{var email=jwt.subject(h.substring(7));users.findByCorreo(email).ifPresent(u->{var a=new UsernamePasswordAuthenticationToken(email,null,List.of(new SimpleGrantedAuthority("ROLE_"+u.getRol().name())));SecurityContextHolder.getContext().setAuthentication(a);});}catch(Exception ignored){}
  chain.doFilter(req,res);
 }
}
