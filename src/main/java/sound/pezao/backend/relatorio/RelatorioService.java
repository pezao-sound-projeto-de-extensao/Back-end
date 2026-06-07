package sound.pezao.backend.relatorio;

import org.hibernate.type.internal.ParameterizedTypeImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import sound.pezao.backend.relatorio.dto.*;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.MovimentacaoRepository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioService {
    private final ItemRepository itemRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public RelatorioService(ItemRepository itemRepository, MovimentacaoRepository movimentacaoRepository) {
        this.itemRepository = itemRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public RelatorioResponseDTO buscar(LocalDate dataInicio, LocalDate dataFim, Integer categoriaId, Pageable pageable) {
        return new RelatorioResponseDTO(
                buscarKpis(),
                buscarItensCriticos(),
                buscarMaisMovimentados(dataInicio, dataFim, categoriaId),
                buscarHistorico(dataInicio, dataFim, categoriaId, pageable)
        );
    }

    private RelatorioKpiDTO buscarKpis() {
        return itemRepository.buscarKpis();
    }

    private List<ItemCriticoDTO> buscarItensCriticos() {
        return itemRepository.buscarItensCriticos();
    }

    private List<ItemMaisMovimentadoDTO> buscarMaisMovimentados(LocalDate dataInicio, LocalDate dataFim, Integer categoriaId) {
        return movimentacaoRepository.buscarMaisMovimentados(
                dataInicio,
                dataFim,
                categoriaId,
                PageRequest.of(0, 5)
        ).getContent();
    }

    private Page<MovimentacaoHistoricoDTO> buscarHistorico(LocalDate dataInicio, LocalDate dataFim, Integer categoriaId, Pageable pageable) {
        return movimentacaoRepository.buscarHistorico(dataInicio, dataFim, categoriaId, pageable);
    }
}
