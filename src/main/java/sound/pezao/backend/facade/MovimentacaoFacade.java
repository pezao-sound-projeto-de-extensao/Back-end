package sound.pezao.backend.facade;

import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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

import java.time.LocalDate;

@Component
public class MovimentacaoFacade {

    private final MovimentacaoService movimentacaoService;
    private final EstoqueService estoqueService;
    private final MovimentacaoMapper mapper;
    private final ItemRepository itemRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public MovimentacaoFacade(
            MovimentacaoService movimentacaoService,
            EstoqueService estoqueService,
            MovimentacaoMapper mapper,
            ItemRepository itemRepository,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.movimentacaoService = movimentacaoService;
        this.estoqueService = estoqueService;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    public Page<MovimentacaoResponse> listar(Integer itemId, String tipo,
                                             Integer usuarioId,
                                             String search,
                                             LocalDate dataInicio,
                                             LocalDate dataFim,
                                             Pageable pageable) {
        // normaliza o tipo para o valor gravado, para "Entrada" filtrar como "entrada"
        String tipoFiltro = tipo != null && !tipo.isBlank()
                ? TipoMovimentacao.fromValor(tipo).getValor()
                : null;

        return mapper.toResponsePage(
                movimentacaoService.listarComFiltros(
                        itemId, tipoFiltro, usuarioId, search, dataInicio, dataFim, pageable)
        );
    }

    public MovimentacaoResponse buscarPorId(Integer id) {
        return mapper.toResponse(
                movimentacaoService.buscarPorId(id)
        );
    }

    @Transactional
    public MovimentacaoResponse registrar(MovimentacaoRequest request) {
        TipoMovimentacao tipo = TipoMovimentacao.fromValor(request.tipo());
        exigirPermissaoPara(tipo);

        Item item = itemRepository.findByIdParaMovimentacao(request.itemId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Item", request.itemId())
                );

        int estoqueAntes = estoqueService.aplicarMovimentacao(
                item,
                tipo,
                request.quantidade()
        );

        Movimentacao movimentacao = mapper.toEntity(request, item);
        movimentacao.setUsuario(usuarioAutenticadoService.obter());
        movimentacao.setEstoqueAntes(estoqueAntes);
        movimentacao.setEstoqueDepois(item.getQuantidadeAtual());

        return mapper.toResponse(
                movimentacaoService.salvar(movimentacao)
        );
    }

    @Transactional
    public void deletar(Integer id) {
        Movimentacao movimentacao = movimentacaoService.buscarPorId(id);

        TipoMovimentacao tipo = TipoMovimentacao.fromValor(movimentacao.getTipo());
        exigirPermissaoPara(tipo);

        Item item = itemRepository.findByIdParaMovimentacao(
                        movimentacao.getItem().getId()
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Item",
                                movimentacao.getItem().getId()
                        )
                );

        estoqueService.reverterMovimentacao(item, tipo, movimentacao.getQuantidade());

        movimentacaoService.deletar(movimentacao);
    }

    @Transactional
    public MovimentacaoResponse uploadNota(
            Integer movimentacaoId,
            MultipartFile arquivo
    ) {
        return mapper.toResponse(
                movimentacaoService.uploadNota(
                        movimentacaoId,
                        arquivo
                )
        );
    }

    public Resource baixarNota(Integer movimentacaoId) {
        return movimentacaoService.baixarNota(movimentacaoId);
    }

    @Transactional
    public void deletarNota(Integer movimentacaoId) {
        movimentacaoService.deletarNota(movimentacaoId);
    }

    /**
     * Entrada e saída são permissões separadas no banco, então a checagem depende
     * do tipo da movimentação e não cabe em uma anotação estática no método.
     */
    private void exigirPermissaoPara(TipoMovimentacao tipo) {
        String permissao = tipo == TipoMovimentacao.ENTRADA ? "REGISTRAR_ENTRADA" : "REGISTRAR_SAIDA";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean autorizado = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(autoridade -> permissao.equals(autoridade.getAuthority()));

        if (!autorizado) {
            throw new AccessDeniedException(
                    "Usuário não tem a permissão " + permissao + " para movimentar o estoque.");
        }
    }
}
