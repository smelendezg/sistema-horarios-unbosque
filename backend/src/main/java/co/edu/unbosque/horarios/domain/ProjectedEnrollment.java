package co.edu.unbosque.horarios.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="matricula_proyectada",uniqueConstraints=@UniqueConstraint(columnNames={"id_periodo","id_curso"}))
public class ProjectedEnrollment {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(optional=false) @JoinColumn(name="id_periodo") private AcademicPeriod periodo;
  @ManyToOne(optional=false) @JoinColumn(name="id_curso") private Course curso;
  @Column(nullable=false) private int demanda;
  @Column(name="fecha_importacion",nullable=false) private LocalDate fechaImportacion=LocalDate.now();
  protected ProjectedEnrollment(){}
  public ProjectedEnrollment(AcademicPeriod periodo,Course curso,int demanda){this.periodo=periodo;this.curso=curso;this.demanda=demanda;}
  public Long getId(){return id;} public AcademicPeriod getPeriodo(){return periodo;} public Course getCurso(){return curso;}
  public int getDemanda(){return demanda;} public LocalDate getFechaImportacion(){return fechaImportacion;}
  public void update(int demanda){this.demanda=demanda;this.fechaImportacion=LocalDate.now();}
}
