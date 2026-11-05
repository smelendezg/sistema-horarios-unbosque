package co.edu.unbosque.horarios.repository;
import co.edu.unbosque.horarios.domain.Schedule; import co.edu.unbosque.horarios.domain.ScheduleStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface ScheduleRepository extends JpaRepository<Schedule,Long>{ Optional<Schedule> findFirstByEstadoOrderByIdDesc(ScheduleStatus estado); }
