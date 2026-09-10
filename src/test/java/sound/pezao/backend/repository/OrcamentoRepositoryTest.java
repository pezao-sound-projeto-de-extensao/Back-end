package sound.pezao.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sound.pezao.backend.entities.Cliente;
import sound.pezao.backend.entities.Orcamento;
import sound.pezao.backend.entities.OrcamentoItem;
import sound.pezao.backend.entities.StatusOrcamento;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Testes de consulta para OrcamentoRepository")
class OrcamentoRepositoryTest {

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final Pageable pageable = PageRequest.of(0, 20);

    private Cliente joao;
    private Cliente maria;

    @BeforeEach
    void setUp() {
        joao = entityManager.persist(new Cliente("João da Silva", "(11) 98888-7777"));
        maria = entityManager.persist(new Cliente("Maria Souza", "(11) 97777-6666"));
    }

    private Orcamento persistirOrcamento(Cliente cliente, StatusOrcamento status, String produto,
                                         int quantidade, double preco) {
        Orcamento orcamento = new Orcamento();
        orcamento.setCliente(cliente);
        orcamento.setStatus(status);

        OrcamentoItem item = new OrcamentoItem();
        item.setDescricao(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(preco);
        orcamento.adicionarItem(item);
        orcamento.recalcularTotal();

        return entityManager.persist(orcamento);
    }

    @Test
    @DisplayName("Deve filtrar por nome do cliente")
    void deveFiltrarPorCliente() {
        persistirOrcamento(joao, StatusOrcamento.PENDENTE, "Caixa selada", 1, 400.0);
        persistirOrcamento(maria, StatusOrcamento.PENDENTE, "Subwoofer", 1, 500.0);

        Page<Orcamento> resultado = orcamentoRepository.findAllFiltered("maria", null, null, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Maria Souza", resultado.getContent().get(0).getCliente().getNome());
    }

    @Test
    @DisplayName("Deve filtrar pelo número do orçamento")
    void deveFiltrarPorNumero() {
        Orcamento primeiro = persistirOrcamento(joao, StatusOrcamento.PENDENTE, "Caixa selada", 1, 400.0);
        persistirOrcamento(maria, StatusOrcamento.PENDENTE, "Subwoofer", 1, 500.0);

        Page<Orcamento> resultado = orcamentoRepository.findAllFiltered(
                String.valueOf(primeiro.getId()), primeiro.getId(), null, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(primeiro.getId(), resultado.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Deve filtrar por status")
    void deveFiltrarPorStatus() {
        persistirOrcamento(joao, StatusOrcamento.PENDENTE, "Caixa selada", 1, 400.0);
        persistirOrcamento(joao, StatusOrcamento.ACEITO, "Subwoofer", 1, 500.0);
        persistirOrcamento(maria, StatusOrcamento.REJEITADO, "Módulo", 1, 300.0);

        assertEquals(1, orcamentoRepository
                .findAllFiltered(null, null, StatusOrcamento.ACEITO, pageable).getTotalElements());
    }

    @Test
    @DisplayName("Deve combinar busca e status")
    void deveCombinarFiltros() {
        persistirOrcamento(joao, StatusOrcamento.PENDENTE, "Caixa selada", 1, 400.0);
        persistirOrcamento(joao, StatusOrcamento.ACEITO, "Subwoofer", 1, 500.0);
        persistirOrcamento(maria, StatusOrcamento.ACEITO, "Módulo", 1, 300.0);

        Page<Orcamento> resultado = orcamentoRepository
                .findAllFiltered("joão", null, StatusOrcamento.ACEITO, pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve devolver tudo sem filtro e ordenar do mais recente para o mais antigo")
    void deveOrdenarDoMaisRecente() {
        Orcamento primeiro = persistirOrcamento(joao, StatusOrcamento.PENDENTE, "Caixa selada", 1, 400.0);
        Orcamento segundo = persistirOrcamento(maria, StatusOrcamento.PENDENTE, "Subwoofer", 1, 500.0);

        List<Integer> ids = orcamentoRepository.findAllFiltered(null, null, null, pageable)
                .getContent().stream().map(Orcamento::getId).toList();

        assertEquals(2, ids.size());
        assertEquals(segundo.getId(), ids.get(0));
        assertEquals(primeiro.getId(), ids.get(1));
    }

    @Test
    @DisplayName("Deve persistir os itens em cascata e guardar o total calculado")
    void devePersistirItensEmCascata() {
        Orcamento orcamento = new Orcamento();
        orcamento.setCliente(joao);
        orcamento.setStatus(StatusOrcamento.PENDENTE);

        OrcamentoItem item = new OrcamentoItem();
        item.setDescricao("Kit de fiação 4mm");
        item.setQuantidade(3);
        item.setPrecoUnitario(50.0);
        orcamento.adicionarItem(item);
        orcamento.recalcularTotal();

        Orcamento salvo = entityManager.persistFlushFind(orcamento);

        assertEquals(150.0, salvo.getValorTotal());
        assertEquals(1, salvo.getItens().size());
        assertTrue(salvo.getItens().get(0).isProdutoNovo());
    }

    @Test
    @DisplayName("Deve devolver página vazia quando nada casa com a busca")
    void deveDevolverPaginaVazia() {
        persistirOrcamento(joao, StatusOrcamento.PENDENTE, "Caixa selada", 1, 400.0);

        assertTrue(orcamentoRepository.findAllFiltered("inexistente", null, null, pageable).isEmpty());
    }
}
