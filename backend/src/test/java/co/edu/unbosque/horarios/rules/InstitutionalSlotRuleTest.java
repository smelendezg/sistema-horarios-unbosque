package co.edu.unbosque.horarios.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import co.edu.unbosque.horarios.service.ParameterService;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InstitutionalSlotRuleTest {
  private ParameterService parameters;
  private InstitutionalSlotRule rule;

  @BeforeEach void setup(){
    parameters=mock(ParameterService.class);
    when(parameters.time(eq("hora_inicio_semana"),any())).thenReturn(LocalTime.of(7,0));
    when(parameters.time(eq("hora_fin_semana"),any())).thenReturn(LocalTime.of(22,0));
    when(parameters.time(eq("hora_fin_sabado"),any())).thenReturn(LocalTime.of(13,0));
    rule=new InstitutionalSlotRule(parameters);
  }

  private AssignmentCandidate candidate(int day,int hour){
    return new AssignmentCandidate(null,null,null,day,LocalTime.of(hour,0),2,List.of(),null);
  }

  @Test @DisplayName("CP-20: rechaza una sesión que cruza el descanso institucional")
  void cp20_rechazaDescansoInstitucional(){
    var issue=rule.validate(candidate(2,11));
    assertTrue(issue.isPresent());
    assertEquals("FRANJA",issue.get().codigo());
  }

  @Test @DisplayName("CP-21: rechaza una sesión que inicia el sábado a las 13:00")
  void cp21_rechazaSabadoDespuesDeLaUna(){
    assertTrue(rule.validate(candidate(6,13)).isPresent());
  }

  @Test @DisplayName("Límite inferior: acepta una sesión que inicia a las 07:00")
  void aceptaLimiteInferior(){assertTrue(rule.validate(candidate(1,7)).isEmpty());}

  @Test @DisplayName("Franja regular: acepta una sesión de 14:00 a 16:00")
  void aceptaFranjaRegular(){assertTrue(rule.validate(candidate(3,14)).isEmpty());}
}
