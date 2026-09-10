package sound.pezao.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sound.pezao.backend.dto.clienteDTO.ClienteRequest;
import sound.pezao.backend.dto.orcamentoDTO.OrcamentoItemRequest;
import sound.pezao.backend.dto.orcamentoDTO.OrcamentoRequest;
import sound.pezao.backend.dto.orcamentoDTO.OrcamentoResponse;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.Cliente;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Orcamento;
import sound.pezao.backend.entities.StatusOrcamento;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.OrcamentoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para OrcamentoService")
class OrcamentoServiceTest {

    @Mock
    private OrcamentoRepository repository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private EncomendaService encomendaService;

    @InjectMocks
    private OrcamentoService service;

    private Cliente cliente;
    private Item amplificador;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("João da Silva", "(11) 98888-7777");
        cliente.setId(1);

        amplificador = new Item();
        amplificador.setId(10);
        amplificador.setNome("Módulo Amplificador 400W");
        amplificador.setCategoria(new Categoria(1, "Som automotivo", LocalDateTime.now()));
        amplificador.setUnidade(new Unidade(1, "Unidade", "UN"));
        amplificador.setUriImagem("imagens/uuid-amplificador.jpg");
    }

    private OrcamentoItemRequest itemCatalogo(Integer itemId, int quantidade, double preco) {
        return new OrcamentoItemRequest(itemId, null, quantidade, preco);
    }

    private OrcamentoItemRequest itemNovo(String descricao, int quantidade, double preco) {
        return new OrcamentoItemRequest(null, descricao, quantidade, preco);
    }

    private OrcamentoRequest requestComClienteExistente(OrcamentoItemRequest... itens) {
        return new OrcamentoRequest(1, null, "Entrega na próxima semana", List.of(itens));
    }

    private void mockarSalvamento() {
        when(repository.save(any(Orcamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioAutenticadoService.obter()).thenReturn(new Usuario());
    }

    @Nested
    @DisplayName("Criação")
    class CriacaoTest {

        @Test
        @DisplayName("Deve criar orçamento para cliente existente e calcular os totais")
        void deveCriarComClienteExistente() {
            when(clienteService.buscarEntidade(1)).thenReturn(cliente);
            when(itemRepository.findById(10)).thenReturn(Optional.of(amplificador));
            mockarSalvamento();

            OrcamentoResponse resposta = service.criar(requestComClienteExistente(
                    itemCatalogo(10, 2, 320.0),
                    itemNovo("Kit de fiação 4mm", 3, 50.0)
            ));

            assertEquals(StatusOrcamento.PENDENTE, resposta.status());
            assertEquals(2, resposta.quantidadeItens());
            assertEquals(790.0, resposta.valorTotal());
            assertEquals(640.0, resposta.itens().get(0).subtotal());
            assertEquals(150.0, resposta.itens().get(1).subtotal());
        }

        @Test
        @DisplayName("Deve cadastrar o cliente novo junto com o orçamento")
        void deveCadastrarClienteNovo() {
            ClienteRequest clienteNovo = new ClienteRequest("Maria Souza", "(11) 97777-6666");
            Cliente salvo = new Cliente("Maria Souza", "(11) 97777-6666");
            salvo.setId(2);

            when(clienteService.criarEntidade(clienteNovo)).thenReturn(salvo);
            mockarSalvamento();

            OrcamentoResponse resposta = service.criar(new OrcamentoRequest(
                    null, clienteNovo, null, List.of(itemNovo("Caixa selada", 1, 400.0))));

            assertEquals("Maria Souza", resposta.cliente().nome());
            verify(clienteService).criarEntidade(clienteNovo);
            verify(clienteService, never()).buscarEntidade(any());
        }

        @Test
        @DisplayName("Deve recusar cliente existente e cliente novo ao mesmo tempo")
        void deveRecusarOsDoisClientes() {
            OrcamentoRequest request = new OrcamentoRequest(
                    1,
                    new ClienteRequest("Maria Souza", "(11) 97777-6666"),
                    null,
                    List.of(itemNovo("Caixa selada", 1, 400.0)));

            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> service.criar(request));

            assertTrue(erro.getMessage().contains("não os dois"));
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Deve recusar orçamento sem nenhum cliente informado")
        void deveRecusarSemCliente() {
            OrcamentoRequest request = new OrcamentoRequest(
                    null, null, null, List.of(itemNovo("Caixa selada", 1, 400.0)));

            assertThrows(IllegalArgumentException.class, () -> service.criar(request));

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Deve recusar item com produto do catálogo e descrição ao mesmo tempo")
        void deveRecusarItemAmbiguo() {
            when(clienteService.buscarEntidade(1)).thenReturn(cliente);
            when(usuarioAutenticadoService.obter()).thenReturn(new Usuario());

            OrcamentoRequest request = new OrcamentoRequest(1, null, null,
                    List.of(new OrcamentoItemRequest(10, "Outro nome", 1, 100.0)));

            assertThrows(IllegalArgumentException.class, () -> service.criar(request));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve recusar item sem produto do catálogo e sem descrição")
        void deveRecusarItemSemIdentificacao() {
            when(clienteService.buscarEntidade(1)).thenReturn(cliente);
            when(usuarioAutenticadoService.obter()).thenReturn(new Usuario());

            OrcamentoRequest request = new OrcamentoRequest(1, null, null,
                    List.of(new OrcamentoItemRequest(null, "   ", 1, 100.0)));

            assertThrows(IllegalArgumentException.class, () -> service.criar(request));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando o produto do catálogo não existe")
        void deveRecusarProdutoInexistente() {
            when(clienteService.buscarEntidade(1)).thenReturn(cliente);
            when(usuarioAutenticadoService.obter()).thenReturn(new Usuario());
            when(itemRepository.findById(99)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.criar(requestComClienteExistente(itemCatalogo(99, 1, 10.0))));
        }

        @Test
        @DisplayName("Deve copiar o nome do produto e marcar o produto novo corretamente")
        void deveDiferenciarProdutoNovoDoCatalogo() {
            when(clienteService.buscarEntidade(1)).thenReturn(cliente);
            when(itemRepository.findById(10)).thenReturn(Optional.of(amplificador));
            mockarSalvamento();

            OrcamentoResponse resposta = service.criar(requestComClienteExistente(
                    itemCatalogo(10, 1, 320.0),
                    itemNovo("Kit de fiação 4mm", 1, 50.0)));

            assertFalse(resposta.itens().get(0).produtoNovo());
            assertEquals("Módulo Amplificador 400W", resposta.itens().get(0).descricao());
            assertEquals("/itens/10/imagem/download", resposta.itens().get(0).fotoUrl());

            assertTrue(resposta.itens().get(1).produtoNovo());
            assertNull(resposta.itens().get(1).itemId());
            assertNull(resposta.itens().get(1).fotoUrl());
        }
    }

    @Nested
    @DisplayName("Edição e transições de status")
    class StatusTest {

        private Orcamento orcamentoPendente() {
            Orcamento orcamento = new Orcamento();
            orcamento.setId(5);
            orcamento.setCliente(cliente);
            orcamento.setStatus(StatusOrcamento.PENDENTE);
            return orcamento;
        }

        @Test
        @DisplayName("Deve aceitar um orçamento pendente e gerar as encomendas")
        void deveAceitarPendente() {
            Orcamento orcamento = orcamentoPendente();
            when(repository.findById(5)).thenReturn(Optional.of(orcamento));
            when(repository.save(orcamento)).thenReturn(orcamento);

            assertEquals(StatusOrcamento.ACEITO, service.aceitar(5).status());

            verify(encomendaService).gerarParaOrcamento(orcamento);
        }

        @Test
        @DisplayName("Não deve gerar encomendas ao rejeitar")
        void naoDeveGerarEncomendasAoRejeitar() {
            Orcamento orcamento = orcamentoPendente();
            when(repository.findById(5)).thenReturn(Optional.of(orcamento));
            when(repository.save(orcamento)).thenReturn(orcamento);

            service.rejeitar(5);

            verifyNoInteractions(encomendaService);
        }

        @Test
        @DisplayName("Deve rejeitar um orçamento pendente")
        void deveRejeitarPendente() {
            Orcamento orcamento = orcamentoPendente();
            when(repository.findById(5)).thenReturn(Optional.of(orcamento));
            when(repository.save(orcamento)).thenReturn(orcamento);

            assertEquals(StatusOrcamento.REJEITADO, service.rejeitar(5).status());
        }

        @Test
        @DisplayName("Não deve aceitar um orçamento que já foi rejeitado")
        void naoDeveAceitarRejeitado() {
            Orcamento orcamento = orcamentoPendente();
            orcamento.setStatus(StatusOrcamento.REJEITADO);
            when(repository.findById(5)).thenReturn(Optional.of(orcamento));

            assertThrows(IllegalArgumentException.class, () -> service.aceitar(5));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Não deve rejeitar um orçamento já aceito")
        void naoDeveRejeitarAceito() {
            Orcamento orcamento = orcamentoPendente();
            orcamento.setStatus(StatusOrcamento.ACEITO);
            when(repository.findById(5)).thenReturn(Optional.of(orcamento));

            assertThrows(IllegalArgumentException.class, () -> service.rejeitar(5));
        }

        @Test
        @DisplayName("Deve substituir os itens ao editar um orçamento pendente")
        void deveEditarPendente() {
            Orcamento orcamento = orcamentoPendente();
            when(repository.findById(5)).thenReturn(Optional.of(orcamento));
            when(clienteService.buscarEntidade(1)).thenReturn(cliente);
            when(repository.save(any(Orcamento.class))).thenAnswer(inv -> inv.getArgument(0));

            OrcamentoResponse resposta = service.atualizar(5, requestComClienteExistente(
                    itemNovo("Caixa selada", 2, 400.0)));

            assertEquals(1, resposta.quantidadeItens());
            assertEquals(800.0, resposta.valorTotal());
        }

        @Test
        @DisplayName("Não deve editar um orçamento que não está pendente")
        void naoDeveEditarAceito() {
            Orcamento orcamento = orcamentoPendente();
            orcamento.setStatus(StatusOrcamento.ACEITO);
            when(repository.findById(5)).thenReturn(Optional.of(orcamento));

            assertThrows(IllegalArgumentException.class,
                    () -> service.atualizar(5, requestComClienteExistente(itemNovo("Caixa", 1, 10.0))));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException para orçamento inexistente")
        void deveLancarExcecaoQuandoNaoExiste() {
            when(repository.findById(99)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> service.buscarPorId(99));
        }
    }

    @Nested
    @DisplayName("Listagem")
    class ListagemTest {

        private final Pageable pageable = PageRequest.of(0, 20);

        private Page<Orcamento> pagina() {
            Orcamento orcamento = new Orcamento();
            orcamento.setId(1);
            orcamento.setCliente(cliente);
            orcamento.setStatus(StatusOrcamento.PENDENTE);
            return new PageImpl<>(List.of(orcamento));
        }

        @Test
        @DisplayName("Deve tratar busca numérica como número do orçamento")
        void deveBuscarPorNumero() {
            when(repository.findAllFiltered("12", 12, null, pageable)).thenReturn(pagina());

            service.listar("12", null, pageable);

            verify(repository).findAllFiltered("12", 12, null, pageable);
        }

        @Test
        @DisplayName("Deve tratar busca textual como nome do cliente")
        void deveBuscarPorCliente() {
            when(repository.findAllFiltered(eq("joão"), isNull(), isNull(), eq(pageable)))
                    .thenReturn(pagina());

            service.listar("joão", null, pageable);

            verify(repository).findAllFiltered("joão", null, null, pageable);
        }

        @Test
        @DisplayName("Deve converter o status informado no filtro")
        void deveFiltrarPorStatus() {
            when(repository.findAllFiltered(isNull(), isNull(), eq(StatusOrcamento.ACEITO), eq(pageable)))
                    .thenReturn(pagina());

            service.listar("  ", "aceito", pageable);

            verify(repository).findAllFiltered(null, null, StatusOrcamento.ACEITO, pageable);
        }

        @Test
        @DisplayName("Deve recusar status inválido no filtro")
        void deveRecusarStatusInvalido() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.listar(null, "arquivado", pageable));

            verifyNoInteractions(repository);
        }
    }
}
