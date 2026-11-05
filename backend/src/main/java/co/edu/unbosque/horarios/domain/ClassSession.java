package co.edu.unbosque.horarios.domain;
import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="sesion")
public class ClassSession {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="id_horario") private Schedule horario;
 @ManyToOne(optional=false) @JoinColumn(name="id_seccion") private Section seccion;
 @ManyToOne(optional=false) @JoinColumn(name="id_aula") private Classroom aula;
 @Column(name="dia_semana",nullable=false) private short diaSemana;
 @Column(name="hora_inicio",nullable=false) private LocalTime horaInicio; @Column(name="hora_fin",nullable=false) private LocalTime horaFin;
 protected ClassSession(){} public ClassSession(Schedule h,Section s,Classroom a,int d,LocalTime i,int duration){horario=h;seccion=s;aula=a;diaSemana=(short)d;horaInicio=i;horaFin=i.plusHours(duration);}
 public Long getId(){return id;} public Schedule getHorario(){return horario;} public Section getSeccion(){return seccion;} public Classroom getAula(){return aula;}
 public int getDiaSemana(){return diaSemana;} public LocalTime getHoraInicio(){return horaInicio;} public LocalTime getHoraFin(){return horaFin;}
 public void move(Classroom a,int d,LocalTime i,int duration){aula=a;diaSemana=(short)d;horaInicio=i;horaFin=i.plusHours(duration);}
}
