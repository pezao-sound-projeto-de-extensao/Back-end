package sound.pezao.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.itemDTO.ItemMapper;
import sound.pezao.backend.dto.itemDTO.ItemRequest;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.entities.Arquivo;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.exception.ArquivoInvalidoException;
import sound.pezao.backend.exception.EntityInativaException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.repository.ArquivoRepository;
import sound.pezao.backend.repository.CategoriaRepository;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.UnidadeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    static final String OBSERVACAO_ESTOQUE_INICIAL = "Estoque inicial do cadastro do produto";

    private final ItemRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UnidadeRepository unidadeRepository;
    private final ArmazenamentoArquivoService armazenamento;
    private final MovimentacaoService movimentacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ArquivoRepository arquivoRepository;

    public ItemService(
            ItemRepository repository,
            CategoriaRepository categoriaRepository,
            UnidadeRepository unidadeRepository,
            ArmazenamentoArquivoService armazenamento,
            MovimentacaoService movimentacaoService,
            UsuarioAutenticadoService usuarioAutenticadoService,
            ArquivoRepository arquivoRepository
    ) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.unidadeRepository = unidadeRepository;
        this.armazenamento = armazenamento;
        this.movimentacaoService = movimentacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.arquivoRepository = arquivoRepository;
    }

    @Transactional
    @PreAuthorize("hasAuthority('CADASTRAR_ITENS')")
    public ItemResponse create(ItemRequest request) {
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new EntityNomeJaExisteException("Item", request.nome());
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria", request.categoriaId()));

        Unidade unidade = unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> new EntityNotFoundException("Unidade", request.unidadeId()));

        Item item = ItemMapper.toEntity(request, categoria, unidade);
        Item salvo = repository.save(item);

        registrarEstoqueInicial(salvo);

        return ItemMapper.toResponse(salvo, arquivoRepository);
    }

    private void registrarEstoqueInicial(Item item) {
        Integer quantidade = item.getQuantidadeAtual();

        if (quantidade == null || quantidade <= 0) {
            return;
        }

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setItem(item);
        movimentacao.setUsuario(usuarioAutenticadoService.obter());
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setEstoqueAntes(0);
        movimentacao.setEstoqueDepois(quantidade);
        movimentacao.setData(LocalDate.now());
        movimentacao.setObservacao(OBSERVACAO_ESTOQUE_INICIAL);

        movimentacaoService.salvar(movimentacao);
    }

    public Page<ItemResponse> findAll(
            Boolean ativo,
            String search,
            Integer categoriaId,
            Boolean apenasAlerta,
            Pageable pageable
    ) {
        Page<Item> pagina = repository.findAllFiltered(
                ativo,
                search,
                categoriaId,
                apenasAlerta,
                pageable
        );

        return pagina.map(item -> ItemMapper.toResponse(item, arquivoRepository));
    }

    public List<ItemResponse> montarRespostas(List<Item> itens) {
        return itens.stream()
                .map(item -> ItemMapper.toResponse(item, arquivoRepository))
                .toList();
    }

    public ItemResponse findById(Integer id) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        return ItemMapper.toResponse(item, arquivoRepository);
    }

    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public ItemResponse update(Integer id, ItemRequest request) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));

        if (!item.getAtivo()) {
            throw new EntityInativaException("Item", id);
        }

        if (!item.getNome().equalsIgnoreCase(request.nome())
                && repository.existsByNomeIgnoreCase(request.nome())) {
            throw new EntityNomeJaExisteException("Item", request.nome());
        }

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria", request.categoriaId()));

        Unidade unidade = unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> new EntityNotFoundException("Unidade", request.unidadeId()));

        item.setNome(request.nome());
        item.setCategoria(categoria);
        item.setUnidade(unidade);

        item.setQuantidadeMinima(request.quantidadeMinima());
        item.setPrecoCusto(request.precoCusto());
        item.setPrecoVenda(request.precoVenda());

        Item salvo = repository.save(item);
        return ItemMapper.toResponse(salvo, arquivoRepository);
    }

    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    @Transactional
    public ItemResponse uploadImagem(Integer itemId, MultipartFile arquivo) {
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item", itemId));

        Optional<Arquivo> arquivoExistente = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo("item", itemId, "imagem");

        String uriAntiga = arquivoExistente.map(Arquivo::getUri).orElse(null);
        String uriNova = armazenamento.salvar(arquivo, "imagens");

        try {
            Arquivo arq = arquivoExistente.orElse(new Arquivo());
            arq.setTabelaOrigem("item");
            arq.setRegistroId(itemId);
            arq.setTipoArquivo("imagem");
            arq.setUri(uriNova);
            arq.setNome(arquivo.getOriginalFilename());
            arq.setMimeType(arquivo.getContentType() != null
                    ? arquivo.getContentType()
                    : "application/octet-stream");
            arq.setTamanho((int) arquivo.getSize());

            arquivoRepository.save(arq);

            if (uriAntiga != null && !uriAntiga.equals(uriNova)) {
                armazenamento.deletar(uriAntiga);
            }

            return ItemMapper.toResponse(item, arquivoRepository);
        } catch (RuntimeException e) {
            armazenamento.deletar(uriNova);
            throw e;
        }
    }

    public Resource baixarImagem(Integer itemId) {
        Arquivo arq = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo("item", itemId, "imagem")
                .orElseThrow(() -> new ArquivoInvalidoException("O item não possui imagem."));

        return armazenamento.carregar(arq.getUri());
    }

    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    public void inativar(Integer id) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        item.setAtivo(false);
        repository.save(item);
    }

    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    public void reativar(Integer id) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        item.setAtivo(true);
        repository.save(item);
    }

    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    @Transactional
    public void deletarImagem(Integer itemId) {
        Arquivo arq = arquivoRepository
                .findByTabelaOrigemAndRegistroIdAndTipoArquivo("item", itemId, "imagem")
                .orElse(null);

        if (arq != null) {
            String uri = arq.getUri();
            arquivoRepository.delete(arq);
            if (uri != null) {
                armazenamento.deletar(uri);
            }
        }
    }
}