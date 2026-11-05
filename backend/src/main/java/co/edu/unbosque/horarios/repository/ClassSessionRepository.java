package co.edu.unbosque.horarios.repository;
import co.edu.unbosque.horarios.domain.ClassSession; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ClassSessionRepository extends JpaRepository<ClassSession,Long>{ List<ClassSession> findByHorarioId(Long horarioId); }
