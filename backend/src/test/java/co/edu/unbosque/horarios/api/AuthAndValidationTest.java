package co.edu.unbosque.horarios.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import co.edu.unbosque.horarios.domain.Role;
import co.edu.unbosque.horarios.domain.User;
import co.edu.unbosque.horarios.repository.ScheduleReviewRepository;
import co.edu.unbosque.horarios.repository.UserRepository;
import co.edu.unbosque.horarios.security.JwtService;
import co.edu.unbosque.horarios.service.ScheduleService;
import jakarta.validation.Validation;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthAndValidationTest {

  @Test @DisplayName("CP-01: permite iniciar sesión con credenciales válidas")
  void cp01_loginValido(){
    var users=mock(UserRepository.class);
    var encoder=mock(PasswordEncoder.class);
    var jwt=mock(JwtService.class);
    var user=new User("Pepita Área","area@unbosque.edu.co","hash",Role.COMITE_AREA);
    when(users.findByCorreo("area@unbosque.edu.co")).thenReturn(Optional.of(user));
    when(encoder.matches("Campus2026!","hash")).thenReturn(true);
    when(jwt.create(user)).thenReturn("token-prueba");

    var response=new AuthController(users,encoder,jwt)
        .login(new ApiDtos.LoginRequest("area@unbosque.edu.co","Campus2026!"));

    assertEquals(200,response.getStatusCode().value());
    var body=(ApiDtos.LoginResponse)response.getBody();
    assertNotNull(body);
    assertEquals("COMITE_AREA",body.rol());
    assertEquals("token-prueba",body.token());
  }

  @Test @DisplayName("CP-02: rechaza contraseña incorrecta sin revelar datos")
  void cp02_loginInvalido(){
    var users=mock(UserRepository.class);
    var encoder=mock(PasswordEncoder.class);
    var jwt=mock(JwtService.class);
    var user=new User("Pepita Área","area@unbosque.edu.co","hash",Role.COMITE_AREA);
    when(users.findByCorreo("area@unbosque.edu.co")).thenReturn(Optional.of(user));
    when(encoder.matches("incorrecta","hash")).thenReturn(false);

    var response=new AuthController(users,encoder,jwt)
        .login(new ApiDtos.LoginRequest("area@unbosque.edu.co","incorrecta"));

    assertEquals(401,response.getStatusCode().value());
    assertEquals("Correo o contraseña incorrectos",((Map<?,?>)response.getBody()).get("message"));
  }

  @Test @DisplayName("CP-03: publicar es exclusivo de Dirección y asignar admite Comité de Área")
  void cp03_permisosPorRol() throws Exception {
    var publish=ScheduleController.class.getMethod("publish",Long.class)
        .getAnnotation(PreAuthorize.class).value();
    var assign=ScheduleController.class.getMethod("assign",Long.class,ApiDtos.AssignRequest.class)
        .getAnnotation(PreAuthorize.class).value();
    assertEquals("hasRole('DIRECCION')",publish);
    assertTrue(assign.contains("COMITE_AREA"));
    assertTrue(assign.contains("DIRECCION"));
  }

  @Test @DisplayName("CP-09: no acepta un aula con capacidad cero")
  void cp09_capacidadDebeSerPositiva(){
    try(var factory=Validation.buildDefaultValidatorFactory()){
      var input=new ApiDtos.ClassroomInput("A-1","Aula",0,false,false);
      assertTrue(factory.getValidator().validate(input).stream()
          .anyMatch(v->v.getPropertyPath().toString().equals("capacidad")));
    }
  }

  @Test @DisplayName("CP-11: la frecuencia semanal máxima permitida es cuatro")
  void cp11_frecuenciaFueraDeRango(){
    try(var factory=Validation.buildDefaultValidatorFactory()){
      var input=new ApiDtos.CourseInput("SIS-999","Prueba",3,5,false,false);
      assertTrue(factory.getValidator().validate(input).stream()
          .anyMatch(v->v.getPropertyPath().toString().equals("frecuenciaSemanal")));
    }
  }
}
