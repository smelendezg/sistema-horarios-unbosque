package co.edu.unbosque.horarios.repository;
import co.edu.unbosque.horarios.domain.ProjectedEnrollment;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProjectedEnrollmentRepository extends JpaRepository<ProjectedEnrollment,Long>{
  List<ProjectedEnrollment> findByPeriodoIdOrderByCursoCodigo(Long periodoId);
  Optional<ProjectedEnrollment> findByPeriodoIdAndCursoId(Long periodoId,Long cursoId);
}
