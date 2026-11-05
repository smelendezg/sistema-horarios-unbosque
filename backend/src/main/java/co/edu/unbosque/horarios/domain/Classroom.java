package co.edu.unbosque.horarios.domain;
import jakarta.persistence.*;
@Entity @Table(name="aula")
public class Classroom {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String codigo; @Column(nullable=false) private String nombre;
 @Column(nullable=false) private int capacidad; @Column(name="tiene_computadores",nullable=false) private boolean computadores;
 @Column(name="tiene_sillas_moviles",nullable=false) private boolean sillasMoviles;
 protected Classroom(){} public Classroom(String c,String n,int cap,boolean pc,boolean sm){codigo=c;nombre=n;capacidad=cap;computadores=pc;sillasMoviles=sm;}
 public Long getId(){return id;} public String getCodigo(){return codigo;} public String getNombre(){return nombre;} public int getCapacidad(){return capacidad;}
 public boolean isComputadores(){return computadores;} public boolean isSillasMoviles(){return sillasMoviles;}
 public void update(String c,String n,int cap,boolean pc,boolean sm){codigo=c;nombre=n;capacidad=cap;computadores=pc;sillasMoviles=sm;}
}
