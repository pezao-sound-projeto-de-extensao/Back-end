package sound.pezao.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.encomendaDTO.EncomendaKpisResponse;
import sound.pezao.backend.dto.encomendaDTO.EncomendaMapper;
import sound.pezao.backend.dto.encomendaDTO.EncomendaResponse;
import sound.pezao.backend.entities.Encomenda;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.Orcamento;
import sound.pezao.backend.entities.OrcamentoItem;
import sound.pezao.backend.entities.StatusEncomenda;
import sound.pezao.backend.entities.StatusOrcamento;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.EncomendaRepository;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.OrcamentoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@PreAuthorize("hasAuthority('GERENCIAR_ENCOMENDAS')")
@Service
public class EncomendaService {

    private final EncomendaRepository repository;
    private final OrcamentoRepository orcamentoRepository;
    private final ItemRepository itemRepository;
    private final EstoqueService estoqueService;
    private final MovimentacaoService movimentacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final EncomendaMapper mapper;

    public EncomendaService(
            EncomendaRepository repository,
            OrcamentoRepository orcamentoRepository,
            ItemRepository itemRepository,
            EstoqueService estoqueService,
            MovimentacaoService movimentacaoService,
            UsuarioAutenticadoService usuarioAutenticadoService,
            EncomendaMapper mapper
    ) {
        this.repository = repository;
        this.orcamentoRepository = orcamentoRepository;
        this.itemRepository = itemRepository;
        this.estoqueService = estoqueService;
        this.movimentacaoService = movimentacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.mapper = mapper;
    }

    public Page<EncomendaResponse> listar(
            String search,
            String status,
            Pageable pageable
    ) {
        String busca = search != null && !search.isBlank()
                ? search.trim()
                : null;

        return repository.findAllFiltered(
                        busca,
                        StatusEncomenda.fromValor(status),
                        pageable
                )
                .map(mapper::toResponse);
    }

    public EncomendaResponse buscarPorId(Integer id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    public EncomendaKpisResponse kpis() {
        LocalDateTime inicioDoMes = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        LocalDateTime fimDoMes = LocalDate.now()
                .withDayOfMonth(LocalDate.now().lengthOfMonth())
                .atTime(LocalTime.MAX);

        return new EncomendaKpisResponse(
                repository.countByStatus(StatusEncomenda.PENDENTE),
                repository.countByStatus(StatusEncomenda.RECEBIDA),
                repository.countByStatusAndConcluidaEmBetween(
                        StatusEncomenda.CONCLUIDA,
                        inicioDoMes,
                        fimDoMes
                )
        );
    }

    @PreAuthorize("permitAll()")
    public List<Encomenda> gerarParaOrcamento(Orcamento orcamento) {
        List<Encomenda> encomendas = orcamento.getItens()
                .stream()
                .map(item -> montar(orcamento, item))
                .toList();

        return repository.saveAll(encomendas);
    }

    @Transactional
    public EncomendaResponse receber(Integer id, Integer itemId) {
        Encomenda encomenda = buscarEntidade(id);

        if (encomenda.getStatus() != StatusEncomenda.PENDENTE) {
            throw new IllegalArgumentException(
                    "Só é possível receber uma encomenda pendente. Esta está como "
                            + encomenda.getStatus() + "."
            );
        }

        Item item = resolverItem(encomenda, itemId);

        registrarMovimentacao(
                encomenda,
                item,
                TipoMovimentacao.ENTRADA,
                "Recebimento da encomenda #" + encomenda.getId()
                        + " do orçamento #" + encomenda.getOrcamento().getId()
        );

        encomenda.setItem(item);
        encomenda.setStatus(StatusEncomenda.RECEBIDA);
        encomenda.setRecebidaEm(LocalDateTime.now());

        return mapper.toResponse(repository.save(encomenda));
    }

    @Transactional
    public EncomendaResponse concluir(Integer id) {
        Encomenda encomenda = buscarEntidade(id);

        if (encomenda.getStatus() != StatusEncomenda.RECEBIDA) {
            throw new IllegalArgumentException(
                    "Só é possível concluir uma encomenda recebida. Esta está como "
                            + encomenda.getStatus() + "."
            );
        }

        registrarMovimentacao(
                encomenda,
                encomenda.getItem(),
                TipoMovimentacao.SAIDA,
                "Entrega da encomenda #" + encomenda.getId()
                        + " do orçamento #" + encomenda.getOrcamento().getId()
        );

        encomenda.setStatus(StatusEncomenda.CONCLUIDA);
        encomenda.setConcluidaEm(LocalDateTime.now());

        Encomenda salva = repository.save(encomenda);
        concluirOrcamentoSeTodasEntregues(encomenda.getOrcamento());

        return mapper.toResponse(salva);
    }

    public Encomenda buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Encomenda", id));
    }

    private Encomenda montar(
            Orcamento orcamento,
            OrcamentoItem orcamentoItem
    ) {
        Encomenda encomenda = new Encomenda();

        encomenda.setOrcamento(orcamento);
        encomenda.setOrcamentoItem(orcamentoItem);
        encomenda.setItem(orcamentoItem.getItem());
        encomenda.setDescricao(orcamentoItem.getDescricao());
        encomenda.setQuantidade(orcamentoItem.getQuantidade());
        encomenda.setStatus(StatusEncomenda.PENDENTE);

        return encomenda;
    }

    private Item resolverItem(Encomenda encomenda, Integer itemId) {
        if (itemId != null) {
            return itemRepository.findById(itemId)
                    .orElseThrow(() -> new EntityNotFoundException("Item", itemId));
        }

        if (encomenda.getItem() == null) {
            throw new IllegalArgumentException(
                    "A encomenda '" + encomenda.getDescricao()
                            + "' é de um produto que não estava cadastrado. "
                            + "Cadastre o produto e informe o itemId ao receber."
            );
        }

        return encomenda.getItem();
    }

    private void registrarMovimentacao(
            Encomenda encomenda,
            Item item,
            TipoMovimentacao tipo,
            String observacao
    ) {
        Item itemComLock = itemRepository
                .findByIdParaMovimentacao(item.getId())
                .orElseThrow(() -> new EntityNotFoundException("Item", item.getId()));

        int estoqueAntes = estoqueService.aplicarMovimentacao(
                itemComLock,
                tipo,
                encomenda.getQuantidade()
        );

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setItem(itemComLock);
        movimentacao.setUsuario(usuarioAutenticadoService.obter());
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(encomenda.getQuantidade());
        movimentacao.setEstoqueAntes(estoqueAntes);
        movimentacao.setEstoqueDepois(itemComLock.getQuantidadeAtual());
        movimentacao.setData(LocalDate.now());
        movimentacao.setObservacao(observacao);

        movimentacaoService.salvar(movimentacao);
    }

    private void concluirOrcamentoSeTodasEntregues(Orcamento orcamento) {
        boolean todasConcluidas = repository
                .findByOrcamento_Id(orcamento.getId())
                .stream()
                .allMatch(encomenda ->
                        encomenda.getStatus() == StatusEncomenda.CONCLUIDA
                );

        if (todasConcluidas) {
            orcamento.setStatus(StatusOrcamento.CONCLUIDO);
            orcamentoRepository.save(orcamento);
        }
    }
}