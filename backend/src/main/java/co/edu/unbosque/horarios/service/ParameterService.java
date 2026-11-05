package co.edu.unbosque.horarios.service;

import co.edu.unbosque.horarios.repository.SystemParameterRepository;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class ParameterService {
  private final SystemParameterRepository parameters;
  public ParameterService(SystemParameterRepository parameters){this.parameters=parameters;}
  public String value(String key,String fallback){return parameters.findByClave(key).map(p->p.getValor()).orElse(fallback);}
  public int integer(String key,int fallback){try{return Integer.parseInt(value(key,String.valueOf(fallback)));}catch(NumberFormatException e){return fallback;}}
  public LocalTime time(String key,LocalTime fallback){try{return LocalTime.parse(value(key,fallback.toString()));}catch(Exception e){return fallback;}}
}
