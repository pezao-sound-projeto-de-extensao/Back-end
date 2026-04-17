package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sound.pezao.backend.entities.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, Integer> {

}
