package co.edu.unbosque.horarios.domain;
import jakarta.persistence.*; import java.time.*; import java.util.*;
@Entity @Table(name="horario")
public class Schedule {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="id_periodo") private AcademicPeriod periodo;
 @Column(nullable=false) private int version=1; @Enumerated(EnumType.STRING) @Column(nullable=false) private ScheduleStatus estado=ScheduleStatus.BORRADOR;
 @Column(name="fecha_creacion",nullable=false) private Instant creado=Instant.now(); @Column(name="fecha_publicacion") private Instant publicado;
 @OneToMany(mappedBy="horario",cascade=CascadeType.ALL,orphanRemoval=true) private List<ClassSession> sesiones=new ArrayList<>();
 protected Schedule(){} public Schedule(AcademicPeriod p){periodo=p;} public Schedule(AcademicPeriod p,int v){periodo=p;version=v;}
 public Long getId(){return id;} public AcademicPeriod getPeriodo(){return periodo;} public int getVersion(){return version;} public ScheduleStatus getEstado(){return estado;}
 public List<ClassSession> getSesiones(){return sesiones;} public void setEstado(ScheduleStatus e){estado=e;if(e==ScheduleStatus.PUBLICADO)publicado=Instant.now();}
}
