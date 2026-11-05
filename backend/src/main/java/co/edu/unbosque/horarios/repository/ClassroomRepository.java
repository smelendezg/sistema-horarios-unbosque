package co.edu.unbosque.horarios.repository;
import co.edu.unbosque.horarios.domain.Classroom; import org.springframework.data.jpa.repository.JpaRepository;
public interface ClassroomRepository extends JpaRepository<Classroom,Long>{}
