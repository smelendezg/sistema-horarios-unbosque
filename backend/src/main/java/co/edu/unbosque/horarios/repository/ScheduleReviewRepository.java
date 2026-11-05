package co.edu.unbosque.horarios.repository;
import co.edu.unbosque.horarios.domain.ScheduleReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ScheduleReviewRepository extends JpaRepository<ScheduleReview,Long>{List<ScheduleReview> findByHorarioIdOrderByFechaDesc(Long horarioId);}
