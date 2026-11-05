package co.edu.unbosque.horarios.repository;
import co.edu.unbosque.horarios.domain.Section; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface SectionRepository extends JpaRepository<Section,Long>{ List<Section> findByPeriodoId(Long periodoId); }
