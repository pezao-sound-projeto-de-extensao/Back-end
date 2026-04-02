package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import sound.pezao.backend.entities.Cargo;

import java.util.Optional;

@Service
public interface CargoRepository extends JpaRepository<Cargo, Integer> {

    Optional<Cargo> findCargoById(int id);
}
