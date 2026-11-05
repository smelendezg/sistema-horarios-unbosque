package co.edu.unbosque.horarios.repository;
import co.edu.unbosque.horarios.domain.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface SystemParameterRepository extends JpaRepository<SystemParameter,Long>{Optional<SystemParameter> findByClave(String clave);}
