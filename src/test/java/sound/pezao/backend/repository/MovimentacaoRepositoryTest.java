package sound.pezao.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.entities.Usuario;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Testes de consulta para MovimentacaoRepository")
class MovimentacaoRepositoryTest {

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final Pageable pageable = PageRequest.of(0, 20);

    private Item amplificador;
    private Item bateria;
    private Usuario operador;
    private Usuario gerente;

    @BeforeEach
    void setUp() {
        Categoria categoria = entityManager.persist(new Categoria(null, "Som automotivo", null));
        Unidade unidade = entityManager.persist(new Unidade(null, "Unidade", "UN"));
        Cargo cargo = entityManager.persist(new Cargo());

        amplificador = persistirItem("Módulo Amplificador 400W", categoria, unidade);
        bateria = persistirItem("Bateria 60Ah", categoria, unidade);

        operador = persistirUsuario("operador@email.com", cargo);
        gerente = persistirUsuario("gerente@email.com", cargo);
    }

    private Item persistirItem(String nome, Categoria categoria, Unidade unidade) {
        Item item = new Item();
        item.setNome(nome);
        item.setCategoria(categoria);
        item.setUnidade(unidade);
        item.setQuantidadeAtual(10);
        item.setQuantidadeMinima(3);
        item.setAtivo(true);
        return entityManager.persist(item);
    }

    private Usuario persistirUsuario(String email, Cargo cargo) {
        Usuario usuario = new Usuario();
        usuario.setNome("Fulano");
        usuario.setEmail(email);
        usuario.setSenhaHash("hash");
        usuario.setCargo(cargo);
        return entityManager.persist(usuario);
    }

    private Movimentacao persistirMovimentacao(Item item, Usuario usuario, TipoMovimentacao tipo,
                                               int quantidade, LocalDate data) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setItem(item);
        movimentacao.setUsuario(usuario);
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setEstoqueAntes(10);
        movimentacao.setEstoqueDepois(10 + quantidade);
        movimentacao.setData(data);
        return entityManager.persist(movimentacao);
    }

    private Page<Movimentacao> buscar(Integer itemId, TipoMovimentacao tipo, Integer usuarioId, String search,
                                      LocalDate inicio, LocalDate fim) {
        return movimentacaoRepository.findWithFilters(itemId, tipo, usuarioId, search, inicio, fim, pageable);
    }

    @Nested
    @DisplayName("Filtro por período")
    class PeriodoTest {

        @Test
        @DisplayName("Deve devolver apenas as movimentações dentro do intervalo")
        void deveFiltrarPorIntervalo() {
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 8, 20));
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.SAIDA, 2, LocalDate.of(2026, 9, 5));
            persistirMovimentacao(bateria, operador, TipoMovimentacao.ENTRADA, 3, LocalDate.of(2026, 9, 25));

            Page<Movimentacao> resultado = buscar(null, null, null, null,
                    LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

            assertEquals(2, resultado.getTotalElements());
        }

        @Test
        @DisplayName("Deve incluir as movimentações nas datas limite do intervalo")
        void deveIncluirLimitesDoIntervalo() {
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 1));
            persistirMovimentacao(bateria, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 30));

            Page<Movimentacao> resultado = buscar(null, null, null, null,
                    LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

            assertEquals(2, resultado.getTotalElements());
        }

        @Test
        @DisplayName("Deve aceitar apenas a data inicial, como no filtro de hoje")
        void deveAceitarApenasDataInicial() {
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 8));
            persistirMovimentacao(bateria, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 1));

            Page<Movimentacao> resultado = buscar(null, null, null, null, LocalDate.of(2026, 9, 8), null);

            assertEquals(1, resultado.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Demais filtros")
    class FiltrosTest {

        @Test
        @DisplayName("Deve buscar por parte do nome do produto ignorando a caixa")
        void deveBuscarPorNomeDoProduto() {
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 5));
            persistirMovimentacao(bateria, operador, TipoMovimentacao.ENTRADA, 3, LocalDate.of(2026, 9, 6));

            Page<Movimentacao> resultado = buscar(null, null, null, "bateria", null, null);

            assertEquals(1, resultado.getTotalElements());
            assertEquals("Bateria 60Ah", resultado.getContent().get(0).getItem().getNome());
        }

        @Test
        @DisplayName("Deve filtrar por tipo, produto e usuário")
        void deveFiltrarPorTipoProdutoEUsuario() {
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 5));
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.SAIDA, 2, LocalDate.of(2026, 9, 6));
            persistirMovimentacao(amplificador, gerente, TipoMovimentacao.SAIDA, 1, LocalDate.of(2026, 9, 7));
            persistirMovimentacao(bateria, operador, TipoMovimentacao.SAIDA, 1, LocalDate.of(2026, 9, 8));

            assertEquals(2, buscar(null, TipoMovimentacao.SAIDA, operador.getId(), null, null, null).getTotalElements());
            assertEquals(1, buscar(amplificador.getId(), TipoMovimentacao.SAIDA, operador.getId(), null, null, null)
                    .getTotalElements());
        }

        @Test
        @DisplayName("Deve devolver tudo quando nenhum filtro é informado")
        void deveDevolverTudoSemFiltro() {
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 5));
            persistirMovimentacao(bateria, gerente, TipoMovimentacao.SAIDA, 1, LocalDate.of(2026, 9, 6));

            assertEquals(2, buscar(null, null, null, null, null, null).getTotalElements());
        }

        @Test
        @DisplayName("Deve devolver página vazia quando nada casa com os filtros")
        void deveDevolverPaginaVazia() {
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 5));

            assertTrue(buscar(null, null, null, "inexistente", null, null).isEmpty());
        }
    }

    @Nested
    @DisplayName("Ordenação e paginação")
    class OrdenacaoTest {

        @Test
        @DisplayName("Deve ordenar da movimentação mais recente para a mais antiga")
        void deveOrdenarDaMaisRecente() {
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 1));
            persistirMovimentacao(bateria, operador, TipoMovimentacao.SAIDA, 2, LocalDate.of(2026, 9, 20));
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 3, LocalDate.of(2026, 9, 10));

            List<LocalDate> datas = buscar(null, null, null, null, null, null)
                    .getContent().stream().map(Movimentacao::getData).toList();

            assertEquals(List.of(
                    LocalDate.of(2026, 9, 20),
                    LocalDate.of(2026, 9, 10),
                    LocalDate.of(2026, 9, 1)), datas);
        }

        @Test
        @DisplayName("Deve paginar o histórico contando o total de registros")
        void devePaginarOHistorico() {
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 5, LocalDate.of(2026, 9, 1));
            persistirMovimentacao(bateria, operador, TipoMovimentacao.SAIDA, 2, LocalDate.of(2026, 9, 2));
            persistirMovimentacao(amplificador, operador, TipoMovimentacao.ENTRADA, 3, LocalDate.of(2026, 9, 3));

            Page<Movimentacao> primeira = movimentacaoRepository.findWithFilters(
                    null, null, null, null, null, null, PageRequest.of(0, 2));

            assertEquals(3, primeira.getTotalElements());
            assertEquals(2, primeira.getTotalPages());
            assertEquals(2, primeira.getContent().size());
            assertEquals(LocalDate.of(2026, 9, 3), primeira.getContent().get(0).getData());
        }
    }
}
