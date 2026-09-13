package sound.pezao.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sound.pezao.backend.dto.encomendaDTO.EncomendaKpisResponse;
import sound.pezao.backend.dto.encomendaDTO.EncomendaMapper;
import sound.pezao.backend.dto.encomendaDTO.EncomendaResponse;
import sound.pezao.backend.entities.Cliente;
import sound.pezao.backend.entities.Encomenda;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.Orcamento;
import sound.pezao.backend.entities.OrcamentoItem;
import sound.pezao.backend.entities.StatusEncomenda;
import sound.pezao.backend.entities.StatusOrcamento;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.exception.EstoqueInsuficienteException;
import sound.pezao.backend.repository.EncomendaRepository;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.OrcamentoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para EncomendaService")
class EncomendaServiceTest {

    @Mock
    private EncomendaRepository repository;

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private MovimentacaoService movimentacaoService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private EncomendaMapper mapper;

    @InjectMocks
    private EncomendaService service;

    private Cliente cliente;
    private Orcamento orcamento;
    private Item amplificador;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("João da Silva", "(11) 98888-7777");
        cliente.setId(1);

        orcamento = new Orcamento();
        orcamento.setId(5);
        orcamento.setCliente(cliente);
        orcamento.setStatus(StatusOrcamento.ACEITO);

        amplificador = new Item();
        amplificador.setId(10);
        amplificador.setNome("Módulo Amplificador 400W");
        amplificador.setQuantidadeAtual(8);
        amplificador.setQuantidadeMinima(3);
    }

    private OrcamentoItem orcamentoItem(
            Item item,
            String descricao,
            int quantidade
    ) {
        OrcamentoItem orcamentoItem = new OrcamentoItem();

        orcamentoItem.setId(100);
        orcamentoItem.setItem(item);
        orcamentoItem.setDescricao(descricao);
        orcamentoItem.setQuantidade(quantidade);
        orcamentoItem.setPrecoUnitario(320.0);

        return orcamentoItem;
    }

    private Encomenda encomenda(
            Integer id,
            Item item,
            StatusEncomenda status,
            int quantidade
    ) {
        Encomenda encomenda = new Encomenda();

        encomenda.setId(id);
        encomenda.setOrcamento(orcamento);
        encomenda.setOrcamentoItem(
                orcamentoItem(item, "Módulo Amplificador 400W", quantidade)
        );
        encomenda.setItem(item);
        encomenda.setDescricao("Módulo Amplificador 400W");
        encomenda.setQuantidade(quantidade);
        encomenda.setStatus(status);

        return encomenda;
    }

    private EncomendaResponse resposta(Encomenda encomenda) {
        Item item = encomenda.getItem();

        return new EncomendaResponse(
                encomenda.getId(),
                encomenda.getDescricao(),
                item != null ? "/itens/" + item.getId() + "/imagem/download" : null,
                item != null ? item.getId() : null,
                encomenda.getQuantidade(),
                encomenda.getOrcamento().getId(),
                encomenda.getOrcamento().getCliente().getNome(),
                encomenda.getStatus(),
                encomenda.getCriadoEm(),
                encomenda.getRecebidaEm(),
                encomenda.getConcluidaEm()
        );
    }

    private void mockarSalvamento() {
        when(repository.save(any(Encomenda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioAutenticadoService.obter()).thenReturn(new Usuario());
        when(mapper.toResponse(any(Encomenda.class)))
                .thenAnswer(invocation -> resposta(invocation.getArgument(0)));
    }

    @Nested
    @DisplayName("Geração a partir do orçamento")
    class GeracaoTest {

        @Test
        @DisplayName("Deve gerar uma encomenda pendente por item do orçamento")
        void deveGerarUmaPorItem() {
            orcamento.adicionarItem(
                    orcamentoItem(amplificador, "Módulo Amplificador 400W", 2)
            );
            orcamento.adicionarItem(
                    orcamentoItem(null, "Kit de fiação 4mm", 3)
            );

            when(repository.saveAll(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            List<Encomenda> geradas = service.gerarParaOrcamento(orcamento);

            assertEquals(2, geradas.size());
            assertTrue(
                    geradas.stream()
                            .allMatch(e -> e.getStatus() == StatusEncomenda.PENDENTE)
            );
            assertEquals(amplificador, geradas.get(0).getItem());
            assertEquals(2, geradas.get(0).getQuantidade());
            assertNull(geradas.get(1).getItem());
            assertEquals("Kit de fiação 4mm", geradas.get(1).getDescricao());
        }

        @Test
        @DisplayName("Deve gerar lista vazia para orçamento sem itens")
        void deveGerarListaVazia() {
            when(repository.saveAll(any()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            assertTrue(service.gerarParaOrcamento(orcamento).isEmpty());
        }
    }

    @Nested
    @DisplayName("Recebimento")
    class RecebimentoTest {

        @Test
        @DisplayName("Deve dar entrada no estoque e marcar como recebida")
        void deveReceber() {
            Encomenda pendente = encomenda(
                    1,
                    amplificador,
                    StatusEncomenda.PENDENTE,
                    2
            );

            when(repository.findById(1)).thenReturn(Optional.of(pendente));
            when(itemRepository.findByIdParaMovimentacao(10))
                    .thenReturn(Optional.of(amplificador));
            when(estoqueService.aplicarMovimentacao(
                    amplificador,
                    TipoMovimentacao.ENTRADA,
                    2
            )).thenAnswer(invocation -> {
                amplificador.setQuantidadeAtual(10);
                return 8;
            });
            mockarSalvamento();

            EncomendaResponse resposta = service.receber(1, null);

            assertEquals(StatusEncomenda.RECEBIDA, resposta.status());
            assertNotNull(resposta.recebidaEm());

            ArgumentCaptor<Movimentacao> captor =
                    ArgumentCaptor.forClass(Movimentacao.class);

            verify(movimentacaoService).salvar(captor.capture());

            Movimentacao movimentacao = captor.getValue();

            assertEquals(TipoMovimentacao.ENTRADA, movimentacao.getTipo());
            assertEquals(2, movimentacao.getQuantidade());
            assertEquals(8, movimentacao.getEstoqueAntes());
            assertEquals(10, movimentacao.getEstoqueDepois());
            assertTrue(movimentacao.getObservacao().contains("orçamento #5"));
        }

        @Test
        @DisplayName("Deve carregar o item com lock antes de movimentar")
        void deveUsarLock() {
            Encomenda pendente = encomenda(
                    1,
                    amplificador,
                    StatusEncomenda.PENDENTE,
                    2
            );

            when(repository.findById(1)).thenReturn(Optional.of(pendente));
            when(itemRepository.findByIdParaMovimentacao(10))
                    .thenReturn(Optional.of(amplificador));
            mockarSalvamento();

            service.receber(1, null);

            verify(itemRepository).findByIdParaMovimentacao(10);
            verify(itemRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Deve recusar encomenda de produto não cadastrado sem itemId")
        void deveRecusarProdutoNaoCadastradoSemItemId() {
            Encomenda pendente = encomenda(
                    1,
                    null,
                    StatusEncomenda.PENDENTE,
                    3
            );

            pendente.setDescricao("Kit de fiação 4mm");

            when(repository.findById(1)).thenReturn(Optional.of(pendente));

            IllegalArgumentException erro = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.receber(1, null)
            );

            assertTrue(erro.getMessage().contains("Kit de fiação 4mm"));
            verifyNoInteractions(estoqueService, movimentacaoService);
        }

        @Test
        @DisplayName("Deve vincular o produto informado ao receber produto não cadastrado")
        void deveVincularProdutoInformado() {
            Encomenda pendente = encomenda(
                    1,
                    null,
                    StatusEncomenda.PENDENTE,
                    3
            );

            when(repository.findById(1)).thenReturn(Optional.of(pendente));
            when(itemRepository.findById(10))
                    .thenReturn(Optional.of(amplificador));
            when(itemRepository.findByIdParaMovimentacao(10))
                    .thenReturn(Optional.of(amplificador));
            mockarSalvamento();

            EncomendaResponse resposta = service.receber(1, 10);

            assertEquals(10, resposta.itemId());
            assertEquals(StatusEncomenda.RECEBIDA, resposta.status());
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando o produto informado não existe")
        void deveRecusarProdutoInexistente() {
            Encomenda pendente = encomenda(
                    1,
                    null,
                    StatusEncomenda.PENDENTE,
                    3
            );

            when(repository.findById(1)).thenReturn(Optional.of(pendente));
            when(itemRepository.findById(99)).thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> service.receber(1, 99)
            );
        }

        @Test
        @DisplayName("Não deve receber uma encomenda que já foi recebida")
        void naoDeveReceberDuasVezes() {
            when(repository.findById(1)).thenReturn(Optional.of(
                    encomenda(1, amplificador, StatusEncomenda.RECEBIDA, 2)
            ));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> service.receber(1, null)
            );

            verifyNoInteractions(estoqueService, movimentacaoService);
        }
    }

    @Nested
    @DisplayName("Conclusão")
    class ConclusaoTest {

        @Test
        @DisplayName("Deve dar saída no estoque e marcar como concluída")
        void deveConcluir() {
            Encomenda recebida = encomenda(
                    1,
                    amplificador,
                    StatusEncomenda.RECEBIDA,
                    2
            );

            when(repository.findById(1)).thenReturn(Optional.of(recebida));
            when(itemRepository.findByIdParaMovimentacao(10))
                    .thenReturn(Optional.of(amplificador));
            when(estoqueService.aplicarMovimentacao(
                    amplificador,
                    TipoMovimentacao.SAIDA,
                    2
            )).thenAnswer(invocation -> {
                amplificador.setQuantidadeAtual(6);
                return 8;
            });
            when(repository.findByOrcamento_Id(5))
                    .thenReturn(List.of(recebida));
            mockarSalvamento();

            EncomendaResponse resposta = service.concluir(1);

            assertEquals(StatusEncomenda.CONCLUIDA, resposta.status());
            assertNotNull(resposta.concluidaEm());

            ArgumentCaptor<Movimentacao> captor =
                    ArgumentCaptor.forClass(Movimentacao.class);

            verify(movimentacaoService).salvar(captor.capture());

            assertEquals(TipoMovimentacao.SAIDA, captor.getValue().getTipo());
        }

        @Test
        @DisplayName("Não deve concluir uma encomenda ainda pendente")
        void naoDeveConcluirPendente() {
            when(repository.findById(1)).thenReturn(Optional.of(
                    encomenda(1, amplificador, StatusEncomenda.PENDENTE, 2)
            ));

            assertThrows(
                    IllegalArgumentException.class,
                    () -> service.concluir(1)
            );

            verifyNoInteractions(estoqueService, movimentacaoService);
        }

        @Test
        @DisplayName("Deve concluir o orçamento quando todas as encomendas forem entregues")
        void deveConcluirOrcamento() {
            Encomenda recebida = encomenda(
                    1,
                    amplificador,
                    StatusEncomenda.RECEBIDA,
                    2
            );

            Encomenda jaConcluida = encomenda(
                    2,
                    amplificador,
                    StatusEncomenda.CONCLUIDA,
                    1
            );

            when(repository.findById(1)).thenReturn(Optional.of(recebida));
            when(itemRepository.findByIdParaMovimentacao(10))
                    .thenReturn(Optional.of(amplificador));
            when(repository.findByOrcamento_Id(5))
                    .thenReturn(List.of(recebida, jaConcluida));
            mockarSalvamento();

            service.concluir(1);

            assertEquals(StatusOrcamento.CONCLUIDO, orcamento.getStatus());
            verify(orcamentoRepository).save(orcamento);
        }

        @Test
        @DisplayName("Não deve concluir o orçamento com encomendas ainda pendentes")
        void naoDeveConcluirOrcamentoComPendencias() {
            Encomenda recebida = encomenda(
                    1,
                    amplificador,
                    StatusEncomenda.RECEBIDA,
                    2
            );

            Encomenda pendente = encomenda(
                    2,
                    amplificador,
                    StatusEncomenda.PENDENTE,
                    1
            );

            when(repository.findById(1)).thenReturn(Optional.of(recebida));
            when(itemRepository.findByIdParaMovimentacao(10))
                    .thenReturn(Optional.of(amplificador));
            when(repository.findByOrcamento_Id(5))
                    .thenReturn(List.of(recebida, pendente));
            mockarSalvamento();

            service.concluir(1);

            assertEquals(StatusOrcamento.ACEITO, orcamento.getStatus());
            verify(orcamentoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Não deve concluir quando o estoque não cobre a saída")
        void naoDeveConcluirSemEstoque() {
            Encomenda recebida = encomenda(
                    1,
                    amplificador,
                    StatusEncomenda.RECEBIDA,
                    2
            );

            when(repository.findById(1)).thenReturn(Optional.of(recebida));
            when(itemRepository.findByIdParaMovimentacao(10))
                    .thenReturn(Optional.of(amplificador));
            doThrow(new EstoqueInsuficienteException(1, 2))
                    .when(estoqueService)
                    .aplicarMovimentacao(
                            amplificador,
                            TipoMovimentacao.SAIDA,
                            2
                    );

            assertThrows(
                    EstoqueInsuficienteException.class,
                    () -> service.concluir(1)
            );

            verify(repository, never()).save(any());
            verify(movimentacaoService, never()).salvar(any());
        }
    }

    @Nested
    @DisplayName("Listagem e indicadores")
    class ListagemTest {

        private final Pageable pageable = PageRequest.of(0, 20);

        @Test
        @DisplayName("Deve repassar busca e status para o repositório")
        void deveRepassarFiltros() {
            Encomenda encomenda = encomenda(
                    1,
                    amplificador,
                    StatusEncomenda.PENDENTE,
                    2
            );

            when(repository.findAllFiltered(
                    "joão",
                    StatusEncomenda.PENDENTE,
                    pageable
            )).thenReturn(new PageImpl<>(List.of(encomenda)));
            when(mapper.toResponse(encomenda)).thenReturn(resposta(encomenda));

            service.listar("joão", "pendente", pageable);

            verify(repository).findAllFiltered(
                    "joão",
                    StatusEncomenda.PENDENTE,
                    pageable
            );
        }

        @Test
        @DisplayName("Deve tratar busca em branco como ausência de filtro")
        void deveTratarBuscaEmBranco() {
            when(repository.findAllFiltered(isNull(), isNull(), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of()));

            service.listar("   ", null, pageable);

            verify(repository).findAllFiltered(null, null, pageable);
        }

        @Test
        @DisplayName("Deve recusar status inválido")
        void deveRecusarStatusInvalido() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> service.listar(null, "entregue", pageable)
            );

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Deve montar os KPIs de pendentes, recebidas e concluídas no mês")
        void deveMontarKpis() {
            when(repository.countByStatus(StatusEncomenda.PENDENTE)).thenReturn(4L);
            when(repository.countByStatus(StatusEncomenda.RECEBIDA)).thenReturn(2L);
            when(repository.countByStatusAndConcluidaEmBetween(
                    eq(StatusEncomenda.CONCLUIDA),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(7L);

            EncomendaKpisResponse kpis = service.kpis();

            assertEquals(4, kpis.pendentes());
            assertEquals(2, kpis.recebidas());
            assertEquals(7, kpis.concluidasNoMes());
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException para encomenda inexistente")
        void deveLancarExcecaoQuandoNaoExiste() {
            when(repository.findById(99)).thenReturn(Optional.empty());

            assertThrows(
                    EntityNotFoundException.class,
                    () -> service.buscarPorId(99)
            );
        }
    }
}