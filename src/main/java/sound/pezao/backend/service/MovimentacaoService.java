package sound.pezao.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.MovimentacaoRepository;

import java.time.LocalDate;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public Movimentacao salvar(Movimentacao movimentacao) {
        return movimentacaoRepository.save(movimentacao);
    }

    public Movimentacao buscarPorId(Integer id) {
        return movimentacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimentação", id));
    }

    public Page<Movimentacao> listarComFiltros(Integer itemId, String tipo,
                                               Integer usuarioId,
                                               String search,
                                               LocalDate dataInicio,
                                               LocalDate dataFim,
                                               Pageable pageable) {
        return movimentacaoRepository.findWithFilters(
                itemId, tipo, usuarioId, search, dataInicio, dataFim, pageable);
    }

    public void deletar(Movimentacao movimentacao) {
        movimentacaoRepository.delete(movimentacao);
    }
}
