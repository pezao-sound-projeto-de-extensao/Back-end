package sound.pezao.backend.service;

import org.springframework.stereotype.Service;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.exception.EstoqueInsuficienteException;
import sound.pezao.backend.repository.ItemRepository;

@Service
public class EstoqueService {

    private final ItemRepository itemRepository;

    public EstoqueService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public int aplicarMovimentacao(Item item, String tipo, Integer quantidade) {
        int estoqueAntes = item.getQuantidadeAtual();

        if (tipo.equals("saida")) {
            if (estoqueAntes < quantidade) {
                throw new EstoqueInsuficienteException(estoqueAntes, quantidade);
            }
            item.setQuantidadeAtual(estoqueAntes - quantidade);
        } else {
            item.setQuantidadeAtual(estoqueAntes + quantidade);
        }

        itemRepository.save(item);
        return estoqueAntes;
    }

    public void reverterMovimentacao(Item item, String tipo, Integer quantidade) {
        if (tipo.equals("entrada")) {
            item.setQuantidadeAtual(item.getQuantidadeAtual() - quantidade);
        } else {
            item.setQuantidadeAtual(item.getQuantidadeAtual() + quantidade);
        }
        itemRepository.save(item);
    }
}
