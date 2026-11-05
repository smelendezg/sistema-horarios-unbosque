package co.edu.unbosque.horarios.domain;
import jakarta.persistence.*;
@Entity @Table(name="seccion")
public class Section {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="id_curso") private Course curso;
 @ManyToOne(optional=false) @JoinColumn(name="id_periodo") private AcademicPeriod periodo;
 @ManyToOne @JoinColumn(name="id_docente") private Teacher docente;
 @Column(nullable=false) private int numero; @Column(name="cupo_maximo",nullable=false) private int cupoMaximo=44;
 @Column(name="estudiantes_estimados",nullable=false) private int estudiantesEstimados; @Column(nullable=false) private String estado="ABIERTA";
 protected Section(){} public Section(Course c,AcademicPeriod p,Teacher d,int n,int estimados){curso=c;periodo=p;docente=d;numero=n;estudiantesEstimados=estimados;}
 public Long getId(){return id;} public Course getCurso(){return curso;} public AcademicPeriod getPeriodo(){return periodo;} public Teacher getDocente(){return docente;} public int getNumero(){return numero;}
 public int getCupoMaximo(){return cupoMaximo;} public int getEstudiantesEstimados(){return estudiantesEstimados;} public String getEstado(){return estado;}
 public void update(Course c,AcademicPeriod p,Teacher d,int n,int estimados){curso=c;periodo=p;docente=d;numero=n;estudiantesEstimados=estimados;}
 public void close(){estado="CERRADA";}
}
