package sound.pezao.backend.service;

import org.springframework.stereotype.Service;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.exception.EstoqueInsuficienteException;
import sound.pezao.backend.repository.ItemRepository;

@Service
public class EstoqueService {

    private final ItemRepository itemRepository;

    public EstoqueService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * Aplica a movimentação no estoque do item e devolve o saldo anterior.
     */
    public int aplicarMovimentacao(Item item, TipoMovimentacao tipo, Integer quantidade) {
        validarQuantidade(quantidade);

        int estoqueAntes = saldoAtual(item);

        if (tipo == TipoMovimentacao.SAIDA) {
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

    /**
     * Desfaz uma movimentação já aplicada. Reverter uma entrada cujo estoque já
     * foi consumido deixaria o saldo negativo, então isso é recusado.
     */
    public void reverterMovimentacao(Item item, TipoMovimentacao tipo, Integer quantidade) {
        validarQuantidade(quantidade);

        int estoqueAtual = saldoAtual(item);

        if (tipo == TipoMovimentacao.ENTRADA) {
            if (estoqueAtual < quantidade) {
                throw new EstoqueInsuficienteException(estoqueAtual, quantidade);
            }
            item.setQuantidadeAtual(estoqueAtual - quantidade);
        } else {
            item.setQuantidadeAtual(estoqueAtual + quantidade);
        }

        itemRepository.save(item);
    }

    private void validarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade movimentada deve ser maior que zero.");
        }
    }

    private int saldoAtual(Item item) {
        return item.getQuantidadeAtual() != null ? item.getQuantidadeAtual() : 0;
    }
}
