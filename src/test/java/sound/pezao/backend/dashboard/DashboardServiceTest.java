package sound.pezao.backend.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sound.pezao.backend.dashboard.dto.DashboardResponse;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.StatusEstoque;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.relatorio.dto.RelatorioKpiDTO;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para DashboardService")
class DashboardServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private DashboardService service;

    private Item item(Integer id, String nome, int quantidadeAtual, int quantidadeMinima) {
        Item item = new Item();
        item.setId(id);
        item.setNome(nome);
        item.setCategoria(new Categoria(1, "Baterias", LocalDateTime.now()));
        item.setUnidade(new Unidade(1, "Unidade", "un"));
        item.setQuantidadeAtual(quantidadeAtual);
        item.setQuantidadeMinima(quantidadeMinima);
        item.setAtivo(true);
        return item;
    }

    private ItemResponse resposta(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getNome(),
                1,
                "Baterias",
                1,
                "Unidade",
                "un",
                item.getQuantidadeAtual(),
                item.getQuantidadeMinima(),
                10.0,
                20.0,
                StatusEstoque.calcular(item.getQuantidadeAtual(), item.getQuantidadeMinima()),
                true,
                null
        );
    }

    @Test
    @DisplayName("Deve montar os KPIs e a lista de atenção")
    void deveMontarKpisEItensAtencao() {
        Item zerado = item(1, "Strobo LED", 0, 2);
        Item baixo = item(2, "Bateria 60Ah", 1, 3);

        when(itemRepository.buscarKpis()).thenReturn(new RelatorioKpiDTO(42L, 30L, 9L, 3L));
        when(itemRepository.buscarItensParaAtencao()).thenReturn(List.of(zerado, baixo));
        when(itemService.montarRespostas(List.of(zerado, baixo)))
                .thenReturn(List.of(resposta(zerado), resposta(baixo)));

        DashboardResponse resposta = service.carregar();

        assertEquals(42, resposta.kpis().totalProdutos());
        assertEquals(30, resposta.kpis().estoqueOk());
        assertEquals(9, resposta.kpis().emAlerta());
        assertEquals(3, resposta.kpis().zerados());
        assertEquals(2, resposta.itensAtencao().size());
        assertTrue(resposta.possuiAlertas());
    }

    @Test
    @DisplayName("Deve devolver os itens zerados antes dos de estoque baixo")
    void deveManterZeradosPrimeiro() {
        Item zerado = item(1, "Strobo LED", 0, 2);
        Item baixo = item(2, "Bateria 60Ah", 1, 3);

        when(itemRepository.buscarKpis()).thenReturn(new RelatorioKpiDTO(2L, 0L, 1L, 1L));
        when(itemRepository.buscarItensParaAtencao()).thenReturn(List.of(zerado, baixo));
        when(itemService.montarRespostas(List.of(zerado, baixo)))
                .thenReturn(List.of(resposta(zerado), resposta(baixo)));

        List<ItemResponse> itens = service.carregar().itensAtencao();

        assertEquals(StatusEstoque.ZERADO, itens.get(0).status());
        assertEquals(StatusEstoque.BAIXO, itens.get(1).status());
    }

    @Test
    @DisplayName("Não deve sinalizar alerta quando nenhum item precisa de atenção")
    void naoDeveSinalizarAlertaSemItensCriticos() {
        when(itemRepository.buscarKpis()).thenReturn(new RelatorioKpiDTO(10L, 10L, 0L, 0L));
        when(itemRepository.buscarItensParaAtencao()).thenReturn(List.of());
        when(itemService.montarRespostas(List.of())).thenReturn(List.of());

        DashboardResponse resposta = service.carregar();

        assertFalse(resposta.possuiAlertas());
        assertTrue(resposta.itensAtencao().isEmpty());
    }

    @Test
    @DisplayName("Deve zerar os KPIs quando não há itens cadastrados")
    void deveZerarKpisQuandoNaoHaItens() {
        when(itemRepository.buscarKpis()).thenReturn(new RelatorioKpiDTO(0L, 0L, 0L, 0L));
        when(itemRepository.buscarItensParaAtencao()).thenReturn(List.of());
        when(itemService.montarRespostas(List.of())).thenReturn(List.of());

        DashboardResponse resposta = service.carregar();

        assertEquals(0, resposta.kpis().totalProdutos());
        assertEquals(0, resposta.kpis().estoqueOk());
        assertEquals(0, resposta.kpis().emAlerta());
        assertEquals(0, resposta.kpis().zerados());
    }

    @Test
    @DisplayName("Deve suportar retorno nulo da consulta de KPIs")
    void deveSuportarKpisNulo() {
        when(itemRepository.buscarKpis()).thenReturn(null);
        when(itemRepository.buscarItensParaAtencao()).thenReturn(List.of());
        when(itemService.montarRespostas(List.of())).thenReturn(List.of());

        assertEquals(0, service.carregar().kpis().totalProdutos());
    }
}