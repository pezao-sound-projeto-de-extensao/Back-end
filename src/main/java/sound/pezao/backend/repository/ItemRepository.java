package sound.pezao.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sound.pezao.backend.entities.Item;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    @Query("""
        SELECT i FROM Item i
        WHERE (:ativo IS NULL OR i.ativo = :ativo)
        AND (:search IS NULL OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Item> findAllFiltered(
            @Param("ativo") Boolean ativo,
            @Param("search") String search,
            Pageable pageable
    );

    boolean existsByNomeIgnoreCase(String nome);
}
