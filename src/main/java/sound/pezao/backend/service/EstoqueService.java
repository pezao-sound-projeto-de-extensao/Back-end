package sound.pezao.backend.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.exception.EstoqueInsuficienteException;
import sound.pezao.backend.repository.ItemRepository;

@Service
public class EstoqueService {

    private static final Logger log = LoggerFactory.getLogger("eventos");

    private final ItemRepository itemRepository;

    public EstoqueService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * Aplica a movimentação no estoque do item e devolve o saldo anterior.
     */
    @WithSpan
    public int aplicarMovimentacao(Item item, TipoMovimentacao tipo, Integer quantidade) {
        validarQuantidade(quantidade);

        int estoqueAntes = saldoAtual(item);

        Span span = Span.current();
        span.setAttribute("item.id", item.getId());
        span.setAttribute("movimentacao.tipo", tipo.name());
        span.setAttribute("movimentacao.quantidade", quantidade);
        span.setAttribute("estoque.antes", estoqueAntes);

        if (tipo == TipoMovimentacao.SAIDA) {
            if (estoqueAntes < quantidade) {
                log.warn("movimentacao_recusada motivo=estoque_insuficiente tipo={} produto={} solicitado={} disponivel={}",
                        tipo, item.getId(), quantidade, estoqueAntes);
                span.setAttribute("movimentacao.recusa", "estoque_insuficiente");
                throw new EstoqueInsuficienteException(estoqueAntes, quantidade);
            }
            item.setQuantidadeAtual(estoqueAntes - quantidade);
        } else {
            item.setQuantidadeAtual(estoqueAntes + quantidade);
        }

        itemRepository.save(item);
        span.setAttribute("estoque.depois", item.getQuantidadeAtual());
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
                log.warn("movimentacao_reversao_recusada motivo=entrada_ja_consumida produto={} quantidade={} disponivel={}",
                        item.getId(), quantidade, estoqueAtual);
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
