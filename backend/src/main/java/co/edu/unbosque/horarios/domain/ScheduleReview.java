package co.edu.unbosque.horarios.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "revision_horario")
public class ScheduleReview {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="id_horario") private Schedule horario;
  @ManyToOne(optional=false) @JoinColumn(name="id_usuario") private User usuario;
  @Column(nullable=false) private String decision;
  private String observaciones;
  @Column(nullable=false) private Instant fecha=Instant.now();

  protected ScheduleReview() {}
  public ScheduleReview(Schedule h,User u,String d,String o){horario=h;usuario=u;decision=d;observaciones=o;}
  public Long getId(){return id;}
  public String getDecision(){return decision;}
  public String getObservaciones(){return observaciones;}
  public Instant getFecha(){return fecha;}
  public User getUsuario(){return usuario;}
}
