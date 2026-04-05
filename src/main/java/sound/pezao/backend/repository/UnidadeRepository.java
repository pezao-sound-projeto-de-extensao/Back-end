package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sound.pezao.backend.entities.Unidade;

public interface UnidadeRepository extends JpaRepository<Unidade, Integer>{
    boolean existsByNomeIgnoreCase(String nome);
}
