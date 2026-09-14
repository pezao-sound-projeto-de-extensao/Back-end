package sound.pezao.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.orcamentoDTO.OrcamentoItemRequest;
import sound.pezao.backend.dto.orcamentoDTO.OrcamentoMapper;
import sound.pezao.backend.dto.orcamentoDTO.OrcamentoRequest;
import sound.pezao.backend.dto.orcamentoDTO.OrcamentoResponse;
import sound.pezao.backend.entities.Cliente;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Orcamento;
import sound.pezao.backend.entities.OrcamentoItem;
import sound.pezao.backend.entities.StatusOrcamento;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.OrcamentoRepository;

@PreAuthorize("hasAuthority('GERENCIAR_ORCAMENTOS')")
@Service
public class OrcamentoService {

    private final OrcamentoRepository repository;
    private final ItemRepository itemRepository;
    private final ClienteService clienteService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final EncomendaService encomendaService;
    private final OrcamentoMapper mapper;

    public OrcamentoService(
            OrcamentoRepository repository,
            ItemRepository itemRepository,
            ClienteService clienteService,
            UsuarioAutenticadoService usuarioAutenticadoService,
            EncomendaService encomendaService,
            OrcamentoMapper mapper
    ) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.clienteService = clienteService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.encomendaService = encomendaService;
        this.mapper = mapper;
    }

    public Page<OrcamentoResponse> listar(
            String search,
            String status,
            Pageable pageable
    ) {
        String busca = search != null && !search.isBlank()
                ? search.trim()
                : null;

        return repository
                .findAllFiltered(
                        busca,
                        extrairNumero(busca),
                        StatusOrcamento.fromValor(status),
                        pageable
                )
                .map(mapper::toResponse);
    }

    public OrcamentoResponse buscarPorId(Integer id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest request) {
        Orcamento orcamento = new Orcamento();

        orcamento.setCliente(resolverCliente(request));
        orcamento.setUsuario(usuarioAutenticadoService.obter());
        orcamento.setStatus(StatusOrcamento.PENDENTE);
        orcamento.setObservacao(request.observacao());

        preencherItens(orcamento, request);

        return mapper.toResponse(repository.save(orcamento));
    }

    @Transactional
    public OrcamentoResponse atualizar(
            Integer id,
            OrcamentoRequest request
    ) {
        Orcamento orcamento = buscarEntidade(id);
        exigirEdicaoPermitida(orcamento);

        orcamento.setCliente(resolverCliente(request));
        orcamento.setObservacao(request.observacao());

        orcamento.limparItens();
        preencherItens(orcamento, request);

        return mapper.toResponse(repository.save(orcamento));
    }

    @Transactional
    public OrcamentoResponse aceitar(Integer id) {
        Orcamento orcamento = alterarStatus(id, StatusOrcamento.ACEITO);

        encomendaService.gerarParaOrcamento(orcamento);

        return mapper.toResponse(orcamento);
    }

    @Transactional
    public OrcamentoResponse rejeitar(Integer id) {
        return mapper.toResponse(
                alterarStatus(id, StatusOrcamento.REJEITADO)
        );
    }

    public Orcamento buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orçamento", id));
    }

    private Orcamento alterarStatus(
            Integer id,
            StatusOrcamento novoStatus
    ) {
        Orcamento orcamento = buscarEntidade(id);

        if (orcamento.getStatus() != StatusOrcamento.PENDENTE) {
            String acao = novoStatus == StatusOrcamento.ACEITO
                    ? "aceitar"
                    : "rejeitar";

            throw new IllegalArgumentException(
                    "Só é possível " + acao
                            + " um orçamento pendente. Este está como "
                            + orcamento.getStatus() + "."
            );
        }

        orcamento.setStatus(novoStatus);

        return repository.save(orcamento);
    }

    private Cliente resolverCliente(OrcamentoRequest request) {
        if (request.temClienteExistente() && request.temClienteNovo()) {
            throw new IllegalArgumentException(
                    "Informe um cliente existente ou os dados de um cliente novo, não os dois."
            );
        }

        if (request.temClienteExistente()) {
            return clienteService.buscarEntidade(request.clienteId());
        }

        if (request.temClienteNovo()) {
            return clienteService.criarEntidade(request.clienteNovo());
        }

        throw new IllegalArgumentException(
                "Informe o cliente do orçamento: um cliente existente ou os dados de um novo."
        );
    }

    private void preencherItens(
            Orcamento orcamento,
            OrcamentoRequest request
    ) {
        request.itens().forEach(itemRequest ->
                orcamento.adicionarItem(montarItem(itemRequest))
        );

        orcamento.recalcularTotal();
    }

    private OrcamentoItem montarItem(OrcamentoItemRequest request) {
        if (request.temProdutoDoCatalogo() && request.temDescricao()) {
            throw new IllegalArgumentException(
                    "Informe o produto do catálogo ou a descrição de um produto novo, não os dois."
            );
        }

        OrcamentoItem item = new OrcamentoItem();

        item.setQuantidade(request.quantidade());
        item.setPrecoUnitario(request.precoUnitario());

        if (request.temProdutoDoCatalogo()) {
            Item produto = itemRepository.findById(request.itemId())
                    .orElseThrow(() ->
                            new EntityNotFoundException("Item", request.itemId())
                    );

            item.setItem(produto);

            // Mantém o nome existente no momento de criação do orçamento.
            item.setDescricao(produto.getNome());

            return item;
        }

        if (!request.temDescricao()) {
            throw new IllegalArgumentException(
                    "Item sem produto do catálogo precisa de uma descrição."
            );
        }

        item.setDescricao(request.descricao().trim());

        return item;
    }

    private void exigirEdicaoPermitida(Orcamento orcamento) {
        if (!orcamento.getStatus().permiteEdicao()) {
            throw new IllegalArgumentException(
                    "Só é possível editar um orçamento pendente. Este está como "
                            + orcamento.getStatus() + "."
            );
        }
    }

    private Integer extrairNumero(String search) {
        if (search == null) {
            return null;
        }

        try {
            return Integer.valueOf(search);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}