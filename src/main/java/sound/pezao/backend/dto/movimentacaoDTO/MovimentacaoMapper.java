package sound.pezao.backend.dto.movimentacaoDTO;

import org.springframework.stereotype.Component;
import sound.pezao.backend.dto.notaEntradaDTO.NotaEntradaMapper;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.NotaEntrada;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.NotaEntradaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MovimentacaoMapper {
    private final ItemRepository itemRepository;
    private final NotaEntradaRepository notaEntradaRepository;

    public MovimentacaoMapper(ItemRepository itemRepository,
                              NotaEntradaRepository notaEntradaRepository) {
        this.itemRepository = itemRepository;
        this.notaEntradaRepository = notaEntradaRepository;
    }

    public MovimentacaoResponse toResponse(Movimentacao movimentacao) {
        return montar(movimentacao,
                notaEntradaRepository.findByMovimentacao_Id(movimentacao.getId()));
    }

    public List<MovimentacaoResponse> toResponseList(List<Movimentacao> movimentacoes) {
        List<Integer> ids = movimentacoes.stream().map(Movimentacao::getId).toList();
        Map<Integer, List<NotaEntrada>> notasPorMovimentacao = ids.isEmpty()
                ? Map.of()
                : notaEntradaRepository.findByMovimentacao_IdIn(ids).stream()
                        .collect(Collectors.groupingBy(nota -> nota.getMovimentacao().getId()));

        return movimentacoes.stream()
                .map(mov -> montar(mov, notasPorMovimentacao.getOrDefault(mov.getId(), List.of())))
                .toList();
    }

    public Movimentacao toEntity(MovimentacaoRequest request) {
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado: ", request.itemId()));

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setItem(item);
        movimentacao.setTipo(request.tipo());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setData(request.data() != null ? request.data() : LocalDate.now());
        movimentacao.setObservacao(request.observacao());
        movimentacao.setCriadoEm(LocalDateTime.now());

        return movimentacao;
    }

    private MovimentacaoResponse montar(Movimentacao movimentacao, List<NotaEntrada> notas) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                new ItemResumoResponse(
                        movimentacao.getItem().getId(),
                        movimentacao.getItem().getNome(),
                        movimentacao.getItem().getCategoria().getId(),
                        movimentacao.getItem().getCategoria().getNome(),
                        movimentacao.getItem().getUnidade().getId(),
                        movimentacao.getItem().getUnidade().getNome(),
                        movimentacao.getItem().getUnidade().getAbreviacao()
                ),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getEstoqueAntes(),
                movimentacao.getEstoqueDepois(),
                movimentacao.getData(),
                movimentacao.getObservacao(),
                movimentacao.getCriadoEm(),
                NotaEntradaMapper.toResponseList(notas)
        );
    }
}
