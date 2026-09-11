package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.exception.EstoqueInsuficienteException;
import sound.pezao.backend.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para EstoqueService")
class EstoqueServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private EstoqueService service;

    private Item item(Integer quantidadeAtual) {
        Item item = new Item();
        item.setId(1);
        item.setQuantidadeAtual(quantidadeAtual);
        return item;
    }

    @Test
    @DisplayName("Deve aplicar entrada somando a quantidade ao estoque")
    void deveAplicarEntrada() {
        Item item = item(10);

        int estoqueAntes = service.aplicarMovimentacao(item, TipoMovimentacao.ENTRADA, 5);

        assertEquals(10, estoqueAntes);
        assertEquals(15, item.getQuantidadeAtual());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve aplicar saída subtraindo a quantidade do estoque")
    void deveAplicarSaida() {
        Item item = item(10);

        int estoqueAntes = service.aplicarMovimentacao(item, TipoMovimentacao.SAIDA, 4);

        assertEquals(10, estoqueAntes);
        assertEquals(6, item.getQuantidadeAtual());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve lançar EstoqueInsuficienteException quando a saída excede o estoque")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        Item item = item(3);

        assertThrows(EstoqueInsuficienteException.class,
                () -> service.aplicarMovimentacao(item, TipoMovimentacao.SAIDA, 5));

        verify(itemRepository, never()).save(item);
    }

    @Test
    @DisplayName("Deve recusar movimentação com quantidade zero ou negativa")
    void deveRecusarQuantidadeNaoPositiva() {
        Item item = item(10);

        assertThrows(IllegalArgumentException.class,
                () -> service.aplicarMovimentacao(item, TipoMovimentacao.ENTRADA, 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.aplicarMovimentacao(item, TipoMovimentacao.SAIDA, -3));

        assertEquals(10, item.getQuantidadeAtual());
        verify(itemRepository, never()).save(item);
    }

    @Test
    @DisplayName("Deve tratar item sem quantidade definida como estoque zero")
    void deveTratarQuantidadeNulaComoZero() {
        Item item = item(null);

        int estoqueAntes = service.aplicarMovimentacao(item, TipoMovimentacao.ENTRADA, 7);

        assertEquals(0, estoqueAntes);
        assertEquals(7, item.getQuantidadeAtual());
    }

    @Test
    @DisplayName("Deve reverter entrada subtraindo a quantidade do estoque")
    void deveReverterEntrada() {
        Item item = item(15);

        service.reverterMovimentacao(item, TipoMovimentacao.ENTRADA, 5);

        assertEquals(10, item.getQuantidadeAtual());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve reverter saída somando a quantidade ao estoque")
    void deveReverterSaida() {
        Item item = item(6);

        service.reverterMovimentacao(item, TipoMovimentacao.SAIDA, 4);

        assertEquals(10, item.getQuantidadeAtual());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve recusar reversão de entrada cujo estoque já foi consumido")
    void deveRecusarReversaoQueDeixariaEstoqueNegativo() {
        Item item = item(2);

        assertThrows(EstoqueInsuficienteException.class,
                () -> service.reverterMovimentacao(item, TipoMovimentacao.ENTRADA, 5));

        assertEquals(2, item.getQuantidadeAtual());
        verify(itemRepository, never()).save(item);
    }
}
