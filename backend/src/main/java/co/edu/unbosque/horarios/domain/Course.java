package co.edu.unbosque.horarios.domain;
import jakarta.persistence.*;
@Entity @Table(name="curso")
public class Course {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String codigo; @Column(nullable=false) private String nombre;
 @Column(nullable=false) private int creditos; @Column(name="frecuencia_semanal",nullable=false) private int frecuenciaSemanal;
 @Column(name="requiere_computadores",nullable=false) private boolean requiereComputadores;
 @Column(name="requiere_sillas_moviles",nullable=false) private boolean requiereSillasMoviles;
 protected Course(){} public Course(String c,String n,int cr,int f,boolean pc,boolean sm){codigo=c;nombre=n;creditos=cr;frecuenciaSemanal=f;requiereComputadores=pc;requiereSillasMoviles=sm;}
 public Long getId(){return id;} public String getCodigo(){return codigo;} public String getNombre(){return nombre;}
 public int getCreditos(){return creditos;} public int getFrecuenciaSemanal(){return frecuenciaSemanal;} public boolean isRequiereComputadores(){return requiereComputadores;} public boolean isRequiereSillasMoviles(){return requiereSillasMoviles;}
 public void update(String c,String n,int cr,int f,boolean pc,boolean sm){codigo=c;nombre=n;creditos=cr;frecuenciaSemanal=f;requiereComputadores=pc;requiereSillasMoviles=sm;}
}
