package sound.pezao.backend.service;

import org.springframework.stereotype.Service;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.MovimentacaoRepository;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<Movimentacao> listarComFiltros(Integer itemId, String tipo,
                                               Integer usuarioId,
                                               LocalDateTime dataInicio,
                                               LocalDateTime dataFim) {
        return movimentacaoRepository.findWithFilters(itemId, tipo, usuarioId, dataInicio, dataFim);
    }

    public void deletar(Movimentacao movimentacao) {
        movimentacaoRepository.delete(movimentacao);
    }
}
