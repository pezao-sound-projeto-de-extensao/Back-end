package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.exception.EstoqueInsuficienteException;
import sound.pezao.backend.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para EstoqueService")
class EstoqueServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private EstoqueService service;

    private Item item(int quantidadeAtual) {
        Item item = new Item();
        item.setId(1);
        item.setQuantidadeAtual(quantidadeAtual);
        return item;
    }

    @Test
    @DisplayName("Deve aplicar entrada somando a quantidade ao estoque")
    void deveAplicarEntrada() {
        Item item = item(10);

        int estoqueAntes = service.aplicarMovimentacao(item, "entrada", 5);

        assertEquals(10, estoqueAntes);
        assertEquals(15, item.getQuantidadeAtual());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve aplicar saída subtraindo a quantidade do estoque")
    void deveAplicarSaida() {
        Item item = item(10);

        int estoqueAntes = service.aplicarMovimentacao(item, "saida", 4);

        assertEquals(10, estoqueAntes);
        assertEquals(6, item.getQuantidadeAtual());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve lançar EstoqueInsuficienteException quando a saída excede o estoque")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        Item item = item(3);

        assertThrows(EstoqueInsuficienteException.class,
                () -> service.aplicarMovimentacao(item, "saida", 5));
    }

    @Test
    @DisplayName("Deve reverter entrada subtraindo a quantidade do estoque")
    void deveReverterEntrada() {
        Item item = item(15);

        service.reverterMovimentacao(item, "entrada", 5);

        assertEquals(10, item.getQuantidadeAtual());
        verify(itemRepository).save(item);
    }

    @Test
    @DisplayName("Deve reverter saída somando a quantidade ao estoque")
    void deveReverterSaida() {
        Item item = item(6);

        service.reverterMovimentacao(item, "saida", 4);

        assertEquals(10, item.getQuantidadeAtual());
        verify(itemRepository).save(item);
    }
}
