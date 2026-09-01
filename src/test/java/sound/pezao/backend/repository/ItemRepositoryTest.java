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
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.relatorio.dto.RelatorioKpiDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Testes de consulta para ItemRepository")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final Pageable pageable = PageRequest.of(0, 20);

    private Categoria somAutomotivo;
    private Categoria baterias;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        somAutomotivo = entityManager.persist(new Categoria(null, "Som automotivo", null));
        baterias = entityManager.persist(new Categoria(null, "Baterias", null));
        unidade = entityManager.persist(new Unidade(null, "Unidade", "UN"));
    }

    private Item persistirItem(String nome, Categoria categoria, int quantidadeAtual,
                               int quantidadeMinima, boolean ativo) {
        Item item = new Item();
        item.setNome(nome);
        item.setCategoria(categoria);
        item.setUnidade(unidade);
        item.setQuantidadeAtual(quantidadeAtual);
        item.setQuantidadeMinima(quantidadeMinima);
        item.setPrecoCusto(10.0);
        item.setPrecoVenda(20.0);
        item.setAtivo(ativo);
        return entityManager.persist(item);
    }

    private List<String> nomes(Page<Item> pagina) {
        return pagina.getContent().stream().map(Item::getNome).toList();
    }

    @Nested
    @DisplayName("Listagem com filtros")
    class FindAllFilteredTest {

        @Test
        @DisplayName("Deve devolver todos os itens quando nenhum filtro é informado")
        void deveDevolverTodosSemFiltro() {
            persistirItem("Amplificador", somAutomotivo, 8, 3, true);
            persistirItem("Bateria 60Ah", baterias, 1, 3, true);
            persistirItem("Tweeter", somAutomotivo, 6, 2, false);

            assertEquals(3, itemRepository.findAllFiltered(null, null, null, null, pageable).getTotalElements());
        }

        @Test
        @DisplayName("Deve filtrar por situação do item")
        void deveFiltrarPorAtivo() {
            persistirItem("Amplificador", somAutomotivo, 8, 3, true);
            persistirItem("Tweeter", somAutomotivo, 6, 2, false);

            assertEquals(List.of("Amplificador"), nomes(itemRepository.findAllFiltered(true, null, null, null, pageable)));
            assertEquals(List.of("Tweeter"), nomes(itemRepository.findAllFiltered(false, null, null, null, pageable)));
        }

        @Test
        @DisplayName("Deve buscar por parte do nome ignorando a caixa")
        void deveBuscarPorNome() {
            persistirItem("Módulo Amplificador 400W", somAutomotivo, 8, 3, true);
            persistirItem("Bateria 60Ah", baterias, 1, 3, true);

            assertEquals(List.of("Módulo Amplificador 400W"),
                    nomes(itemRepository.findAllFiltered(null, "amplificador", null, null, pageable)));
        }

        @Test
        @DisplayName("Deve filtrar por categoria")
        void deveFiltrarPorCategoria() {
            persistirItem("Amplificador", somAutomotivo, 8, 3, true);
            persistirItem("Bateria 60Ah", baterias, 1, 3, true);

            assertEquals(List.of("Bateria 60Ah"),
                    nomes(itemRepository.findAllFiltered(null, null, baterias.getId(), null, pageable)));
        }

        @Test
        @DisplayName("Deve devolver apenas itens em alerta quando apenasAlerta é true")
        void deveFiltrarApenasAlerta() {
            persistirItem("Amplificador", somAutomotivo, 8, 3, true);   // ok
            persistirItem("Subwoofer", somAutomotivo, 2, 2, true);      // no limite
            persistirItem("Bateria 60Ah", baterias, 1, 3, true);        // abaixo do mínimo
            persistirItem("Strobo LED", somAutomotivo, 0, 2, true);     // zerado

            List<String> encontrados = nomes(itemRepository.findAllFiltered(null, null, null, true, pageable));

            assertEquals(3, encontrados.size());
            assertTrue(encontrados.containsAll(List.of("Subwoofer", "Bateria 60Ah", "Strobo LED")));
        }

        @Test
        @DisplayName("Deve ignorar o filtro de alerta quando ele vem falso")
        void deveIgnorarFiltroDeAlertaQuandoFalso() {
            persistirItem("Amplificador", somAutomotivo, 8, 3, true);
            persistirItem("Strobo LED", somAutomotivo, 0, 2, true);

            assertEquals(2, itemRepository.findAllFiltered(null, null, null, false, pageable).getTotalElements());
        }

        @Test
        @DisplayName("Deve combinar categoria, alerta e situação")
        void deveCombinarFiltros() {
            persistirItem("Bateria 60Ah", baterias, 1, 3, true);
            persistirItem("Bateria 45Ah", baterias, 9, 3, true);
            persistirItem("Bateria antiga", baterias, 0, 3, false);
            persistirItem("Strobo LED", somAutomotivo, 0, 2, true);

            assertEquals(List.of("Bateria 60Ah"),
                    nomes(itemRepository.findAllFiltered(true, null, baterias.getId(), true, pageable)));
        }
    }

    @Nested
    @DisplayName("Itens que precisam de atenção")
    class ItensParaAtencaoTest {

        @Test
        @DisplayName("Deve trazer os zerados primeiro e ordenar do menor saldo para o maior")
        void deveOrdenarZeradosPrimeiro() {
            persistirItem("Amplificador", somAutomotivo, 8, 3, true);
            persistirItem("Subwoofer", somAutomotivo, 2, 2, true);
            persistirItem("Bateria 60Ah", baterias, 1, 3, true);
            persistirItem("Strobo LED", somAutomotivo, 0, 2, true);

            List<Item> itens = itemRepository.buscarItensParaAtencao();

            assertEquals(List.of("Strobo LED", "Bateria 60Ah", "Subwoofer"),
                    itens.stream().map(Item::getNome).toList());
        }

        @Test
        @DisplayName("Não deve trazer itens inativos")
        void naoDeveTrazerInativos() {
            persistirItem("Tweeter", somAutomotivo, 0, 2, false);

            assertTrue(itemRepository.buscarItensParaAtencao().isEmpty());
        }
    }

    @Nested
    @DisplayName("KPIs do estoque")
    class KpisTest {

        @Test
        @DisplayName("Deve contar total, ok, em alerta e zerados apenas entre os ativos")
        void deveContarKpis() {
            persistirItem("Amplificador", somAutomotivo, 8, 3, true);   // ok
            persistirItem("Cabo RCA", somAutomotivo, 15, 5, true);      // ok
            persistirItem("Bateria 60Ah", baterias, 1, 3, true);        // alerta
            persistirItem("Strobo LED", somAutomotivo, 0, 2, true);     // zerado
            persistirItem("Tweeter", somAutomotivo, 6, 2, false);       // inativo

            RelatorioKpiDTO kpis = itemRepository.buscarKpis();

            assertEquals(4L, kpis.totalItens());
            assertEquals(2L, kpis.itensOk());
            assertEquals(1L, kpis.itensAlerta());
            assertEquals(1L, kpis.itensZerados());
        }
    }
}
