package sound.pezao.backend.dto.movimentacaoDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import sound.pezao.backend.dto.imagemProdutoDTO.ImagemProdutoMapper;
import sound.pezao.backend.dto.notaEntradaDTO.NotaEntradaMapper;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.NotaEntrada;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.repository.ImagemProdutoRepository;
import sound.pezao.backend.repository.NotaEntradaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MovimentacaoMapper {
    private final NotaEntradaRepository notaEntradaRepository;
    private final ImagemProdutoRepository imagemProdutoRepository;

    public MovimentacaoMapper(NotaEntradaRepository notaEntradaRepository,
                              ImagemProdutoRepository imagemProdutoRepository) {
        this.notaEntradaRepository = notaEntradaRepository;
        this.imagemProdutoRepository = imagemProdutoRepository;
    }

    public MovimentacaoResponse toResponse(Movimentacao movimentacao) {
        return montar(movimentacao,
                notaEntradaRepository.findByMovimentacao_Id(movimentacao.getId()),
                buscarFotos(List.of(movimentacao)));
    }

    public List<MovimentacaoResponse> toResponseList(List<Movimentacao> movimentacoes) {
        List<Integer> ids = movimentacoes.stream().map(Movimentacao::getId).toList();
        Map<Integer, List<NotaEntrada>> notasPorMovimentacao = ids.isEmpty()
                ? Map.of()
                : notaEntradaRepository.findByMovimentacao_IdIn(ids).stream()
                        .collect(Collectors.groupingBy(nota -> nota.getMovimentacao().getId()));

        Map<Integer, String> fotosPorItem = buscarFotos(movimentacoes);

        return movimentacoes.stream()
                .map(mov -> montar(mov,
                        notasPorMovimentacao.getOrDefault(mov.getId(), List.of()),
                        fotosPorItem))
                .toList();
    }

    public Page<MovimentacaoResponse> toResponsePage(Page<Movimentacao> movimentacoes) {
        List<MovimentacaoResponse> conteudo = toResponseList(movimentacoes.getContent());

        return new PageImpl<>(conteudo, movimentacoes.getPageable(), movimentacoes.getTotalElements());
    }

    /**
     * Uma consulta só para as fotos de todos os itens da página, em vez de uma por
     * linha do histórico. Vale a primeira imagem cadastrada de cada item.
     */
    private Map<Integer, String> buscarFotos(List<Movimentacao> movimentacoes) {
        List<Integer> itemIds = movimentacoes.stream()
                .map(mov -> mov.getItem().getId())
                .distinct()
                .toList();

        if (itemIds.isEmpty()) {
            return Map.of();
        }

        return imagemProdutoRepository.findByItem_IdIn(itemIds).stream()
                .collect(Collectors.toMap(
                        imagem -> imagem.getItem().getId(),
                        imagem -> ImagemProdutoMapper.toResponse(imagem).url(),
                        (primeira, segunda) -> primeira));
    }

    /**
     * O item vem carregado pelo chamador, para não perder o lock aplicado na
     * leitura feita dentro da transação da movimentação.
     */
    public Movimentacao toEntity(MovimentacaoRequest request, Item item) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setItem(item);
        movimentacao.setTipo(TipoMovimentacao.fromValor(request.tipo()).getValor());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setData(request.data() != null ? request.data() : LocalDate.now());
        movimentacao.setObservacao(request.observacao());
        movimentacao.setCriadoEm(LocalDateTime.now());

        return movimentacao;
    }

    private MovimentacaoResponse montar(Movimentacao movimentacao, List<NotaEntrada> notas,
                                        Map<Integer, String> fotosPorItem) {
        Item item = movimentacao.getItem();

        return new MovimentacaoResponse(
                movimentacao.getId(),
                new ItemResumoResponse(
                        item.getId(),
                        item.getNome(),
                        item.getCategoria().getId(),
                        item.getCategoria().getNome(),
                        item.getUnidade().getId(),
                        item.getUnidade().getNome(),
                        item.getUnidade().getAbreviacao(),
                        fotosPorItem.get(item.getId())
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
