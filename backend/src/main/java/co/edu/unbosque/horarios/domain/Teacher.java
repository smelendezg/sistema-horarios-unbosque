package co.edu.unbosque.horarios.domain;
import jakarta.persistence.*;
@Entity @Table(name="docente")
public class Teacher {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="id_externo",unique=true) private String idExterno;
 @Column(nullable=false) private String nombre; @Column(nullable=false) private String correo;
 @Column(name="tipo_vinculacion",nullable=false) private String vinculacion; @Column(nullable=false) private String escalafon;
 protected Teacher(){} public Teacher(String ext,String n,String c,String v,String e){idExterno=ext;nombre=n;correo=c;vinculacion=v;escalafon=e;}
 public Long getId(){return id;} public String getIdExterno(){return idExterno;} public String getNombre(){return nombre;} public String getCorreo(){return correo;}
 public String getVinculacion(){return vinculacion;} public String getEscalafon(){return escalafon;}
 public void update(String ext,String n,String c,String v,String e){idExterno=ext;nombre=n;correo=c;vinculacion=v;escalafon=e;}
}
