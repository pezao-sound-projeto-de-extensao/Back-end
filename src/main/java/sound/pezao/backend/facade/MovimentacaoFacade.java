package sound.pezao.backend.facade;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoMapper;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoRequest;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoResponse;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.service.EstoqueService;
import sound.pezao.backend.service.MovimentacaoService;
import sound.pezao.backend.service.UsuarioAutenticadoService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MovimentacaoFacade {

    private final MovimentacaoService movimentacaoService;
    private final EstoqueService estoqueService;
    private final MovimentacaoMapper mapper;
    private final ItemRepository itemRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public MovimentacaoFacade(MovimentacaoService movimentacaoService,
                              EstoqueService estoqueService,
                              MovimentacaoMapper mapper,
                              ItemRepository itemRepository,
                              UsuarioAutenticadoService usuarioAutenticadoService) {
        this.movimentacaoService = movimentacaoService;
        this.estoqueService = estoqueService;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    public List<MovimentacaoResponse> listar(Integer itemId, String tipo,
                                             Integer usuarioId,
                                             LocalDateTime dataInicio,
                                             LocalDateTime dataFim) {
        return mapper.toResponseList(
                movimentacaoService.listarComFiltros(itemId, tipo, usuarioId, dataInicio, dataFim)
        );
    }

    public MovimentacaoResponse buscarPorId(Integer id) {
        return mapper.toResponse(movimentacaoService.buscarPorId(id));
    }

    @Transactional
    public MovimentacaoResponse registrar(MovimentacaoRequest request) {
        TipoMovimentacao tipo = TipoMovimentacao.fromValor(request.tipo());

        Item item = itemRepository.findByIdParaMovimentacao(request.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item", request.itemId()));

        int estoqueAntes = estoqueService.aplicarMovimentacao(item, tipo, request.quantidade());

        Movimentacao movimentacao = mapper.toEntity(request, item);
        movimentacao.setUsuario(usuarioAutenticadoService.obter());
        movimentacao.setEstoqueAntes(estoqueAntes);
        movimentacao.setEstoqueDepois(item.getQuantidadeAtual());

        return mapper.toResponse(movimentacaoService.salvar(movimentacao));
    }

    @Transactional
    public void deletar(Integer id) {
        Movimentacao movimentacao = movimentacaoService.buscarPorId(id);

        Item item = itemRepository.findByIdParaMovimentacao(movimentacao.getItem().getId())
                .orElseThrow(() -> new EntityNotFoundException("Item", movimentacao.getItem().getId()));

        estoqueService.reverterMovimentacao(
                item,
                TipoMovimentacao.fromValor(movimentacao.getTipo()),
                movimentacao.getQuantidade()
        );

        movimentacaoService.deletar(movimentacao);
    }
}
