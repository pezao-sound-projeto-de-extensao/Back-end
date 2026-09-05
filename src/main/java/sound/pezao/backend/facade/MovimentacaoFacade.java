package sound.pezao.backend.facade;

import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
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
    private final ItemRepository itemRepository;
    private final UsuarioRepository usuarioRepository;

    public MovimentacaoFacade(
            MovimentacaoService movimentacaoService,
            EstoqueService estoqueService,
            ItemRepository itemRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.movimentacaoService = movimentacaoService;
        this.estoqueService = estoqueService;
        this.itemRepository = itemRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<MovimentacaoResponse> listar(
            Integer itemId,
            String tipo,
            Integer usuarioId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        return movimentacaoService
                .listarComFiltros(
                        itemId,
                        tipo,
                        usuarioId,
                        dataInicio,
                        dataFim
                )
                .stream()
                .map(MovimentacaoMapper::toResponse)
                .toList();
    }

    public MovimentacaoResponse buscarPorId(Integer id) {
        return MovimentacaoMapper.toResponse(
                movimentacaoService.buscarPorId(id)
        );
    }

    @Transactional
    public MovimentacaoResponse registrar(
            MovimentacaoRequest request
    ) {
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Item",
                                request.itemId()
                        )
                );

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Usuário",
                                0
                        )
                );

        int estoqueAntes = estoqueService.aplicarMovimentacao(
                item,
                request.tipo(),
                request.quantidade()
        );

        Movimentacao movimentacao = MovimentacaoMapper.toEntity(request);
        movimentacao.setUsuario(usuario);
        movimentacao.setItem(item);
        movimentacao.setEstoqueAntes(estoqueAntes);
        movimentacao.setEstoqueDepois(item.getQuantidadeAtual());

        return MovimentacaoMapper.toResponse(
                movimentacaoService.salvar(movimentacao)
        );
    }

    @Transactional
    public void deletar(Integer id) {
        Movimentacao movimentacao =
                movimentacaoService.buscarPorId(id);

        estoqueService.reverterMovimentacao(
                movimentacao.getItem(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade()
        );

        movimentacaoService.deletar(movimentacao);
    }

    public MovimentacaoResponse uploadNota(
            Integer movimentacaoId,
            MultipartFile arquivo
    ) {
        return MovimentacaoMapper.toResponse(
                movimentacaoService.uploadNota(
                        movimentacaoId,
                        arquivo
                )
        );
    }

    public Resource baixarNota(Integer movimentacaoId) {
        return movimentacaoService.baixarNota(movimentacaoId);
    }

    public void deletarNota(Integer movimentacaoId) {
        movimentacaoService.deletarNota(movimentacaoId);
    }
}