package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sound.pezao.backend.dto.itemDTO.ItemRequest;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityInativaException;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.CategoriaRepository;
import sound.pezao.backend.repository.ImagemProdutoRepository;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.UnidadeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ItemService")
class ItemServiceTest {

    @Mock
    private ItemRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UnidadeRepository unidadeRepository;

    @Mock
    private ImagemProdutoRepository imagemProdutoRepository;

    @Mock
    private MovimentacaoService movimentacaoService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private ItemService service;

    private ItemRequest request(String nome) {
        return new ItemRequest(nome, 1, 1, 5, 3, 180.0, 320.0);
    }

    private ItemRequest request(String nome, Integer quantidadeAtual) {
        return new ItemRequest(nome, 1, 1, quantidadeAtual, 3, 180.0, 320.0);
    }

    private Item item(Integer id, String nome, boolean ativo) {
        Item item = new Item();
        item.setId(id);
        item.setNome(nome);
        item.setCategoria(categoria());
        item.setUnidade(unidade());
        item.setQuantidadeAtual(5);
        item.setQuantidadeMinima(3);
        item.setPrecoCusto(180.0);
        item.setPrecoVenda(320.0);
        item.setAtivo(ativo);
        return item;
    }

    private Categoria categoria() {
        return new Categoria(1, "Áudio", LocalDateTime.now());
    }

    private Unidade unidade() {
        return new Unidade(1, "Unidade", "un");
    }

    @Test
    @DisplayName("Deve criar item com sucesso")
    void deveCriarItemComSucesso() {
        ItemRequest request = request("Amplificador");
        when(repository.existsByNomeIgnoreCase("Amplificador")).thenReturn(false);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(unidadeRepository.findById(1)).thenReturn(Optional.of(unidade()));
        when(repository.save(any(Item.class))).thenReturn(item(1, "Amplificador", true));
        when(usuarioAutenticadoService.obter()).thenReturn(new Usuario());

        ItemResponse resposta = service.create(request);

        assertNotNull(resposta);
        assertEquals("Amplificador", resposta.nome());
    }

    @Test
    @DisplayName("Deve registrar movimentação de entrada com o estoque inicial ao criar item")
    void deveRegistrarMovimentacaoDeEstoqueInicialAoCriar() {
        Usuario usuarioLogado = new Usuario();
        Item salvo = item(1, "Amplificador", true);
        salvo.setQuantidadeAtual(5);

        when(repository.existsByNomeIgnoreCase("Amplificador")).thenReturn(false);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(unidadeRepository.findById(1)).thenReturn(Optional.of(unidade()));
        when(repository.save(any(Item.class))).thenReturn(salvo);
        when(usuarioAutenticadoService.obter()).thenReturn(usuarioLogado);

        service.create(request("Amplificador", 5));

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoService).salvar(captor.capture());

        Movimentacao movimentacao = captor.getValue();
        assertEquals(TipoMovimentacao.ENTRADA.getValor(), movimentacao.getTipo());
        assertEquals(5, movimentacao.getQuantidade());
        assertEquals(0, movimentacao.getEstoqueAntes());
        assertEquals(5, movimentacao.getEstoqueDepois());
        assertEquals(salvo, movimentacao.getItem());
        assertEquals(usuarioLogado, movimentacao.getUsuario());
        assertEquals(LocalDate.now(), movimentacao.getData());
        assertNotNull(movimentacao.getObservacao());
    }

    @Test
    @DisplayName("Não deve gerar movimentação ao criar item com estoque zerado")
    void naoDeveGerarMovimentacaoQuandoEstoqueInicialZero() {
        Item salvo = item(1, "Amplificador", true);
        salvo.setQuantidadeAtual(0);

        when(repository.existsByNomeIgnoreCase("Amplificador")).thenReturn(false);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(unidadeRepository.findById(1)).thenReturn(Optional.of(unidade()));
        when(repository.save(any(Item.class))).thenReturn(salvo);

        service.create(request("Amplificador", 0));

        verify(movimentacaoService, never()).salvar(any());
        verify(usuarioAutenticadoService, never()).obter();
    }

    @Test
    @DisplayName("Deve lançar EntityNomeJaExisteException ao criar item com nome existente")
    void deveLancarExcecaoQuandoNomeJaExisteNaCriacao() {
        when(repository.existsByNomeIgnoreCase("Amplificador")).thenReturn(true);

        assertThrows(EntityNomeJaExisteException.class, () -> service.create(request("Amplificador")));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao criar item com categoria inexistente")
    void deveLancarExcecaoQuandoCategoriaInexistente() {
        when(repository.existsByNomeIgnoreCase("Amplificador")).thenReturn(false);
        when(categoriaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create(request("Amplificador")));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao criar item com unidade inexistente")
    void deveLancarExcecaoQuandoUnidadeInexistente() {
        when(repository.existsByNomeIgnoreCase("Amplificador")).thenReturn(false);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(unidadeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create(request("Amplificador")));
    }

    @Test
    @DisplayName("Deve listar itens paginados com suas imagens")
    void deveListarItensPaginados() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> pagina = new PageImpl<>(List.of(item(1, "Amplificador", true)));
        when(repository.findAllFiltered(null, null, pageable)).thenReturn(pagina);
        when(imagemProdutoRepository.findByItem_IdIn(anyList())).thenReturn(List.of());

        Page<ItemResponse> resultado = service.findAll(null, null, pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há itens")
    void deveRetornarPaginaVaziaQuandoNaoHaItens() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAllFiltered(null, null, pageable)).thenReturn(Page.empty(pageable));

        Page<ItemResponse> resultado = service.findAll(null, null, pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve buscar item por id com sucesso")
    void deveBuscarItemPorId() {
        when(repository.findById(1)).thenReturn(Optional.of(item(1, "Amplificador", true)));
        when(imagemProdutoRepository.findByItem_Id(1)).thenReturn(List.of());

        ItemResponse resposta = service.findById(1);

        assertEquals(1, resposta.id());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao buscar item inexistente")
    void deveLancarExcecaoAoBuscarItemInexistente() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(99));
    }

    @Test
    @DisplayName("Deve atualizar item com sucesso")
    void deveAtualizarItemComSucesso() {
        when(repository.findById(1)).thenReturn(Optional.of(item(1, "Amplificador", true)));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(unidadeRepository.findById(1)).thenReturn(Optional.of(unidade()));
        when(repository.save(any(Item.class))).thenReturn(item(1, "Amplificador Novo", true));
        when(imagemProdutoRepository.findByItem_Id(1)).thenReturn(List.of());

        ItemResponse resposta = service.update(1, request("Amplificador Novo"));

        assertEquals("Amplificador Novo", resposta.nome());
    }

    @Test
    @DisplayName("Não deve alterar o saldo em estoque na atualização do item")
    void naoDeveAlterarQuantidadeAtualNaAtualizacao() {
        Item existente = item(1, "Amplificador", true);
        existente.setQuantidadeAtual(7);

        when(repository.findById(1)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(unidadeRepository.findById(1)).thenReturn(Optional.of(unidade()));
        when(repository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(imagemProdutoRepository.findByItem_Id(1)).thenReturn(List.of());

        // o request pede 99 unidades; o saldo tem que continuar 7
        ItemResponse resposta = service.update(1, request("Amplificador", 99));

        assertEquals(7, resposta.quantidadeAtual());
        assertEquals(7, existente.getQuantidadeAtual());
        verify(movimentacaoService, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar item inexistente")
    void deveLancarExcecaoAoAtualizarItemInexistente() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(99, request("Amplificador")));
    }

    @Test
    @DisplayName("Deve lançar EntityInativaException ao atualizar item inativo")
    void deveLancarExcecaoAoAtualizarItemInativo() {
        when(repository.findById(1)).thenReturn(Optional.of(item(1, "Amplificador", false)));

        assertThrows(EntityInativaException.class, () -> service.update(1, request("Amplificador")));
    }

    @Test
    @DisplayName("Deve lançar EntityNomeJaExisteException ao atualizar para nome existente")
    void deveLancarExcecaoQuandoNomeJaExisteNaAtualizacao() {
        when(repository.findById(1)).thenReturn(Optional.of(item(1, "Amplificador", true)));
        when(repository.existsByNomeIgnoreCase("Outro Nome")).thenReturn(true);

        assertThrows(EntityNomeJaExisteException.class, () -> service.update(1, request("Outro Nome")));
    }

    @Test
    @DisplayName("Deve inativar item com sucesso")
    void deveInativarItemComSucesso() {
        Item item = item(1, "Amplificador", true);
        when(repository.findById(1)).thenReturn(Optional.of(item));

        service.inativar(1);

        assertFalse(item.getAtivo());
        verify(repository).save(item);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao inativar item inexistente")
    void deveLancarExcecaoAoInativarItemInexistente() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.inativar(99));
    }

    @Test
    @DisplayName("Deve reativar item com sucesso")
    void deveReativarItemComSucesso() {
        Item item = item(1, "Amplificador", false);
        when(repository.findById(1)).thenReturn(Optional.of(item));

        service.reativar(1);

        assertTrue(item.getAtivo());
        verify(repository).save(item);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao reativar item inexistente")
    void deveLancarExcecaoAoReativarItemInexistente() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.reativar(99));
    }
}
