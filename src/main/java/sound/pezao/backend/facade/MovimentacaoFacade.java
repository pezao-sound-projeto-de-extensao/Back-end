package sound.pezao.backend.facade;

import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoMapper;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoRequest;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoResponse;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.UsuarioRepository;
import sound.pezao.backend.service.EstoqueService;
import sound.pezao.backend.service.MovimentacaoService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MovimentacaoFacade {

    private final MovimentacaoService movimentacaoService;
    private final EstoqueService estoqueService;
    private final MovimentacaoMapper mapper;
    private final ItemRepository itemRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimentacaoFacade(MovimentacaoService movimentacaoService,
                              EstoqueService estoqueService,
                              MovimentacaoMapper mapper,
                              ItemRepository itemRepository,
                              UsuarioRepository usuarioRepository) {
        this.movimentacaoService = movimentacaoService;
        this.estoqueService = estoqueService;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.usuarioRepository = usuarioRepository;
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
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item não encontrado: ", request.itemId()));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", 0));

        int estoqueAntes = estoqueService.aplicarMovimentacao(item, request.tipo(), request.quantidade());

        Movimentacao movimentacao = mapper.toEntity(request);
        movimentacao.setUsuario(usuario);
        movimentacao.setEstoqueAntes(estoqueAntes);
        movimentacao.setEstoqueDepois(item.getQuantidadeAtual());

        return mapper.toResponse(movimentacaoService.salvar(movimentacao));
    }

    @Transactional
    public void deletar(Integer id) {
        Movimentacao movimentacao = movimentacaoService.buscarPorId(id);

        estoqueService.reverterMovimentacao(
                movimentacao.getItem(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade()
        );

        movimentacaoService.deletar(movimentacao);
    }
}
