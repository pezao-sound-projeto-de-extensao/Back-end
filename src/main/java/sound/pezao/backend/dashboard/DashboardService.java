package sound.pezao.backend.dashboard;

import org.springframework.stereotype.Service;
import sound.pezao.backend.dashboard.dto.DashboardKpisResponse;
import sound.pezao.backend.dashboard.dto.DashboardResponse;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.relatorio.dto.RelatorioKpiDTO;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.service.ItemService;

import java.util.List;

@Service
public class DashboardService {

    private final ItemRepository itemRepository;
    private final ItemService itemService;

    public DashboardService(ItemRepository itemRepository, ItemService itemService) {
        this.itemRepository = itemRepository;
        this.itemService = itemService;
    }

    public DashboardResponse carregar() {
        DashboardKpisResponse kpis = montarKpis(itemRepository.buscarKpis());
        List<ItemResponse> itensAtencao = itemService.montarRespostas(itemRepository.buscarItensParaAtencao());

        return new DashboardResponse(kpis, itensAtencao, !itensAtencao.isEmpty());
    }

    private DashboardKpisResponse montarKpis(RelatorioKpiDTO kpis) {
        if (kpis == null) {
            return new DashboardKpisResponse(0, 0, 0, 0);
        }

        return new DashboardKpisResponse(
                valor(kpis.totalItens()),
                valor(kpis.itensOk()),
                valor(kpis.itensAlerta()),
                valor(kpis.itensZerados())
        );
    }

    // As agregações vêm nulas quando não há nenhum item cadastrado.
    private long valor(Long total) {
        return total != null ? total : 0L;
    }
}
