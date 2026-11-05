package co.edu.unbosque.horarios.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "parametro")
public class SystemParameter {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, unique = true) private String clave;
  @Column(nullable = false) private String valor;
  private String unidad;
  private String descripcion;

  protected SystemParameter() {}
  public Long getId(){return id;}
  public String getClave(){return clave;}
  public String getValor(){return valor;}
  public String getUnidad(){return unidad;}
  public String getDescripcion(){return descripcion;}
  public void setValor(String valor){this.valor=valor;}
}
