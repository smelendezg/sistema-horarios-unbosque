package co.edu.unbosque.horarios.domain;
import jakarta.persistence.*; import java.time.LocalDate;
@Entity @Table(name="periodo_academico")
public class AcademicPeriod {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false,unique=true) private String codigo;
  @Column(name="fecha_inicio",nullable=false) private LocalDate fechaInicio;
  @Column(name="fecha_fin",nullable=false) private LocalDate fechaFin;
  protected AcademicPeriod(){} public AcademicPeriod(String c,LocalDate i,LocalDate f){codigo=c;fechaInicio=i;fechaFin=f;}
  public Long getId(){return id;} public String getCodigo(){return codigo;}
}
