package co.edu.unbosque.horarios.repository;
import co.edu.unbosque.horarios.domain.Course; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface CourseRepository extends JpaRepository<Course,Long>{Optional<Course> findByCodigo(String codigo);}
