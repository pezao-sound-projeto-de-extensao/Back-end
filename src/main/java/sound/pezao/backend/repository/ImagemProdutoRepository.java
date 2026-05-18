package sound.pezao.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sound.pezao.backend.entities.ImagemProduto;

import java.util.Collection;
import java.util.List;

public interface ImagemProdutoRepository extends JpaRepository<ImagemProduto, Integer> {

    List<ImagemProduto> findByItem_Id(Integer itemId);

    List<ImagemProduto> findByItem_IdIn(Collection<Integer> itemIds);
}
