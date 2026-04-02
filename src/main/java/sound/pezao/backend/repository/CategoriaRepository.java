package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sound.pezao.backend.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer>{

}
