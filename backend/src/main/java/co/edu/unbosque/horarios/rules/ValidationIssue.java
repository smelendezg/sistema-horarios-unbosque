package co.edu.unbosque.horarios.rules;
public record ValidationIssue(Level nivel,String codigo,String mensaje){ public enum Level{ERROR,ADVERTENCIA} }
