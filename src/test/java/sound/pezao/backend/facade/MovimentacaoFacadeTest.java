package sound.pezao.backend.facade;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoMapper;
import sound.pezao.backend.dto.movimentacaoDTO.MovimentacaoRequest;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.exception.EstoqueInsuficienteException;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.service.EstoqueService;
import sound.pezao.backend.service.MovimentacaoService;
import sound.pezao.backend.service.UsuarioAutenticadoService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para MovimentacaoFacade")
class MovimentacaoFacadeTest {

    @Mock
    private MovimentacaoService movimentacaoService;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private MovimentacaoMapper mapper;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private MovimentacaoFacade facade;

    private Item item;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1);
        item.setQuantidadeAtual(10);

        usuario = new Usuario();

        autenticarCom("REGISTRAR_ENTRADA", "REGISTRAR_SAIDA");
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarCom(String... permissoes) {
        List<SimpleGrantedAuthority> autoridades = Arrays.stream(permissoes)
                .map(SimpleGrantedAuthority::new)
                .toList();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("operador@email.com", null, autoridades));
    }

    private MovimentacaoRequest request(String tipo, Integer quantidade) {
        return new MovimentacaoRequest(1, tipo, quantidade, LocalDate.now(), "NF 1042");
    }

    @Test
    @DisplayName("Deve registrar movimentação com autor e saldos antes e depois")
    void deveRegistrarMovimentacao() {
        MovimentacaoRequest request = request("entrada", 5);
        Movimentacao movimentacao = new Movimentacao();

        when(itemRepository.findByIdParaMovimentacao(1)).thenReturn(Optional.of(item));
        when(estoqueService.aplicarMovimentacao(item, TipoMovimentacao.ENTRADA, 5)).thenAnswer(invocation -> {
            item.setQuantidadeAtual(15);
            return 10;
        });
        when(mapper.toEntity(request, item)).thenReturn(movimentacao);
        when(usuarioAutenticadoService.obter()).thenReturn(usuario);
        when(movimentacaoService.salvar(movimentacao)).thenReturn(movimentacao);

        facade.registrar(request);

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoService).salvar(captor.capture());

        Movimentacao salva = captor.getValue();
        assertEquals(usuario, salva.getUsuario());
        assertEquals(10, salva.getEstoqueAntes());
        assertEquals(15, salva.getEstoqueDepois());
    }

    @Test
    @DisplayName("Deve carregar o item com lock antes de aplicar a movimentação")
    void deveCarregarItemComLock() {
        MovimentacaoRequest request = request("saida", 2);

        when(itemRepository.findByIdParaMovimentacao(1)).thenReturn(Optional.of(item));
        when(mapper.toEntity(request, item)).thenReturn(new Movimentacao());
        when(usuarioAutenticadoService.obter()).thenReturn(usuario);

        facade.registrar(request);

        verify(itemRepository).findByIdParaMovimentacao(1);
        verify(itemRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve recusar tipo inválido antes de tocar no estoque")
    void deveRecusarTipoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.registrar(request("transferencia", 5)));

        verifyNoInteractions(itemRepository, estoqueService, movimentacaoService);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o item não existe")
    void deveLancarExcecaoQuandoItemNaoExiste() {
        when(itemRepository.findByIdParaMovimentacao(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> facade.registrar(request("entrada", 5)));

        verifyNoInteractions(estoqueService, movimentacaoService);
    }

    @Test
    @DisplayName("Deve reverter o estoque ao excluir a movimentação")
    void deveReverterEstoqueAoExcluir() {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId(7);
        movimentacao.setItem(item);
        movimentacao.setTipo("entrada");
        movimentacao.setQuantidade(4);

        when(movimentacaoService.buscarPorId(7)).thenReturn(movimentacao);
        when(itemRepository.findByIdParaMovimentacao(1)).thenReturn(Optional.of(item));

        facade.deletar(7);

        verify(estoqueService).reverterMovimentacao(item, TipoMovimentacao.ENTRADA, 4);
        verify(movimentacaoService).deletar(movimentacao);
    }

    @Test
    @DisplayName("Deve recusar entrada quando o usuário não tem REGISTRAR_ENTRADA")
    void deveRecusarEntradaSemPermissao() {
        autenticarCom("REGISTRAR_SAIDA");

        assertThrows(AccessDeniedException.class, () -> facade.registrar(request("entrada", 5)));

        verifyNoInteractions(itemRepository, estoqueService, movimentacaoService);
    }

    @Test
    @DisplayName("Deve recusar saída quando o usuário não tem REGISTRAR_SAIDA")
    void deveRecusarSaidaSemPermissao() {
        autenticarCom("REGISTRAR_ENTRADA");

        assertThrows(AccessDeniedException.class, () -> facade.registrar(request("saida", 5)));

        verifyNoInteractions(itemRepository, estoqueService, movimentacaoService);
    }

    @Test
    @DisplayName("Deve recusar movimentação quando não há usuário autenticado")
    void deveRecusarSemAutenticacao() {
        SecurityContextHolder.clearContext();

        assertThrows(AccessDeniedException.class, () -> facade.registrar(request("entrada", 5)));
    }

    @Test
    @DisplayName("Deve exigir a permissão do tipo da movimentação ao excluir")
    void deveExigirPermissaoDoTipoAoExcluir() {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId(7);
        movimentacao.setItem(item);
        movimentacao.setTipo("saida");
        movimentacao.setQuantidade(4);

        autenticarCom("REGISTRAR_ENTRADA");
        when(movimentacaoService.buscarPorId(7)).thenReturn(movimentacao);

        assertThrows(AccessDeniedException.class, () -> facade.deletar(7));

        verify(movimentacaoService, never()).deletar(any());
        verifyNoInteractions(estoqueService);
    }

    @Test
    @DisplayName("Deve normalizar o tipo informado no filtro do histórico")
    void deveNormalizarTipoNoFiltro() {
        Pageable pageable = PageRequest.of(0, 20);
        LocalDate inicio = LocalDate.of(2026, 9, 1);
        LocalDate fim = LocalDate.of(2026, 9, 30);

        when(movimentacaoService.listarComFiltros(1, "entrada", 2, "cabo", inicio, fim, pageable))
                .thenReturn(Page.empty(pageable));

        facade.listar(1, "ENTRADA", 2, "cabo", inicio, fim, pageable);

        verify(movimentacaoService).listarComFiltros(1, "entrada", 2, "cabo", inicio, fim, pageable);
    }

    @Test
    @DisplayName("Deve tratar tipo em branco como ausência de filtro")
    void deveTratarTipoEmBrancoComoSemFiltro() {
        Pageable pageable = PageRequest.of(0, 20);

        when(movimentacaoService.listarComFiltros(null, null, null, null, null, null, pageable))
                .thenReturn(Page.empty(pageable));

        facade.listar(null, "  ", null, null, null, null, pageable);

        verify(movimentacaoService).listarComFiltros(null, null, null, null, null, null, pageable);
    }

    @Test
    @DisplayName("Deve recusar tipo inválido informado no filtro")
    void deveRecusarTipoInvalidoNoFiltro() {
        Pageable pageable = PageRequest.of(0, 20);

        assertThrows(IllegalArgumentException.class,
                () -> facade.listar(null, "transferencia", null, null, null, null, pageable));

        verifyNoInteractions(movimentacaoService);
    }

    @Test
    @DisplayName("Não deve excluir a movimentação quando a reversão é recusada")
    void naoDeveExcluirQuandoReversaoFalha() {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId(7);
        movimentacao.setItem(item);
        movimentacao.setTipo("entrada");
        movimentacao.setQuantidade(50);

        when(movimentacaoService.buscarPorId(7)).thenReturn(movimentacao);
        when(itemRepository.findByIdParaMovimentacao(1)).thenReturn(Optional.of(item));
        doThrow(new EstoqueInsuficienteException(10, 50))
                .when(estoqueService).reverterMovimentacao(eq(item), eq(TipoMovimentacao.ENTRADA), eq(50));

        assertThrows(EstoqueInsuficienteException.class, () -> facade.deletar(7));

        verify(movimentacaoService, never()).deletar(any());
    }
}
