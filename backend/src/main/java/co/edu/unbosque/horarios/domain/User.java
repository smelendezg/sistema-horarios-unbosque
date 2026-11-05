package co.edu.unbosque.horarios.domain;

import jakarta.persistence.*;

@Entity @Table(name="usuario")
public class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false) private String nombre;
  @Column(nullable=false, unique=true) private String correo;
  @Column(name="hash_clave", nullable=false) private String passwordHash;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Role rol;
  @Column(nullable=false) private boolean activo=true;
  protected User() {}
  public User(String nombre,String correo,String passwordHash,Role rol){this.nombre=nombre;this.correo=correo;this.passwordHash=passwordHash;this.rol=rol;}
  public Long getId(){return id;} public String getNombre(){return nombre;} public String getCorreo(){return correo;}
  public String getPasswordHash(){return passwordHash;} public Role getRol(){return rol;} public boolean isActivo(){return activo;}
  public void setRol(Role rol){this.rol=rol;}
  public void setActivo(boolean activo){this.activo=activo;}
}
