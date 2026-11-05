package co.edu.unbosque.horarios.config;

import co.edu.unbosque.horarios.security.JwtFilter;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean PasswordEncoder encoder(){return new BCryptPasswordEncoder();}

  @Bean
  SecurityFilterChain chain(HttpSecurity http, JwtFilter filter,
                            @Value("${app.cors-origin}") String origin) throws Exception {
    return http
      .csrf(csrf -> csrf.disable())
      .cors(cors -> cors.configurationSource(request -> {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        return config;
      }))
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(errors -> errors
        .authenticationEntryPoint((request,response,exception) -> jsonError(response,401,"La sesión no es válida o ha terminado"))
        .accessDeniedHandler((request,response,exception) -> jsonError(response,403,"Tu perfil no tiene permiso para realizar esta acción")))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/error","/api/auth/**","/api/public/**","/swagger-ui/**","/v3/api-docs/**").permitAll()
        .requestMatchers(HttpMethod.GET,"/api/**").authenticated()
        .anyRequest().authenticated())
      .addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class)
      .build();
  }

  private static void jsonError(jakarta.servlet.http.HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write("{\"message\":\""+message+"\"}");
  }
}
