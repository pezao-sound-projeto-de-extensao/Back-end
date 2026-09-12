package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.itemDTO.ItemRequest;
import sound.pezao.backend.dto.itemDTO.ItemResponse;
import sound.pezao.backend.entities.Arquivo;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.entities.Item;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.StatusEstoque;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.ArquivoInvalidoException;
import sound.pezao.backend.exception.EntityInativaException;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ArquivoRepository;
import sound.pezao.backend.repository.CategoriaRepository;
import sound.pezao.backend.repository.ItemRepository;
import sound.pezao.backend.repository.UnidadeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ItemService")
class ItemServiceTest {

    private static final String TABELA_ORIGEM_ITEM = "item";
    private static final String TIPO_ARQUIVO_IMAGEM = "imagem";

    @Mock
    private ItemRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UnidadeRepository unidadeRepository;

    @Mock
    private ArmazenamentoArquivoService armazenamento;

    @Mock
    private MovimentacaoService movimentacaoService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private ArquivoRepository arquivoRepository;

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

    private Arquivo imagem(Integer itemId, String uri) {
        Arquivo arquivo = new Arquivo();
        arquivo.setId(1);
        arquivo.setTabelaOrigem(TABELA_ORIGEM_ITEM);
        arquivo.setRegistroId(itemId);
        arquivo.setTipoArquivo(TIPO_ARQUIVO_IMAGEM);
        arquivo.setUri(uri);
        arquivo.setNome("foto.jpg");
        arquivo.setMimeType("image/jpeg");
        arquivo.setTamanho(8);
        arquivo.setCriadoEm(LocalDateTime.now());
        return arquivo;
    }

    private Categoria categoria() {
        return new Categoria(1, "Áudio", LocalDateTime.now());
    }

    private Unidade unidade() {
        return new Unidade(1, "Unidade", "un");
    }

    private void semImagem(Integer itemId) {
        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_ITEM,
                itemId,
                TIPO_ARQUIVO_IMAGEM
        )).thenReturn(Optional.empty());
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
        semImagem(1);

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
        semImagem(1);

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
        semImagem(1);

        service.create(request("Amplificador", 0));

        verify(movimentacaoService, never()).salvar(any());
        verify(usuarioAutenticadoService, never()).obter();
    }

    @Test
    @DisplayName("Deve lançar EntityNomeJaExisteException ao criar item com nome existente")
    void deveLancarExcecaoQuandoNomeJaExisteNaCriacao() {
        when(repository.existsByNomeIgnoreCase("Amplificador")).thenReturn(true);

        assertThrows(
                EntityNomeJaExisteException.class,
                () -> service.create(request("Amplificador"))
        );
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao criar item com categoria inexistente")
    void deveLancarExcecaoQuandoCategoriaInexistente() {
        when(repository.existsByNomeIgnoreCase("Amplificador")).thenReturn(false);
        when(categoriaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> service.create(request("Amplificador"))
        );
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao criar item com unidade inexistente")
    void deveLancarExcecaoQuandoUnidadeInexistente() {
        when(repository.existsByNomeIgnoreCase("Amplificador")).thenReturn(false);
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(unidadeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> service.create(request("Amplificador"))
        );
    }

    @Test
    @DisplayName("Deve listar itens paginados")
    void deveListarItensPaginados() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> pagina = new PageImpl<>(List.of(item(1, "Amplificador", true)));

        when(repository.findAllFiltered(null, null, null, null, pageable))
                .thenReturn(pagina);
        semImagem(1);

        Page<ItemResponse> resultado = service.findAll(
                null,
                null,
                null,
                null,
                pageable
        );

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve repassar os filtros de categoria e alerta para o repositório")
    void deveRepassarFiltrosDeCategoriaEAlerta() {
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAllFiltered(true, "amp", 2, true, pageable))
                .thenReturn(Page.empty(pageable));

        service.findAll(true, "amp", 2, true, pageable);

        verify(repository).findAllFiltered(true, "amp", 2, true, pageable);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há itens")
    void deveRetornarPaginaVaziaQuandoNaoHaItens() {
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAllFiltered(null, null, null, null, pageable))
                .thenReturn(Page.empty(pageable));

        Page<ItemResponse> resultado = service.findAll(
                null,
                null,
                null,
                null,
                pageable
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve calcular o status do estoque em cada item da listagem")
    void deveCalcularStatusNaListagem() {
        Item zerado = item(1, "Strobo", true);
        zerado.setQuantidadeAtual(0);
        zerado.setQuantidadeMinima(2);

        Item baixo = item(2, "Bateria", true);
        baixo.setQuantidadeAtual(2);
        baixo.setQuantidadeMinima(3);

        Item ok = item(3, "Cabo", true);
        ok.setQuantidadeAtual(15);
        ok.setQuantidadeMinima(5);

        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAllFiltered(null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(zerado, baixo, ok)));
        semImagem(1);
        semImagem(2);
        semImagem(3);

        List<ItemResponse> itens = service.findAll(
                null,
                null,
                null,
                null,
                pageable
        ).getContent();

        assertEquals(StatusEstoque.ZERADO, itens.get(0).status());
        assertEquals(StatusEstoque.BAIXO, itens.get(1).status());
        assertEquals(StatusEstoque.OK, itens.get(2).status());
    }

    @Test
    @DisplayName("Deve montar respostas de uma lista")
    void deveMontarRespostasDeUmaLista() {
        List<Item> itens = List.of(
                item(1, "Amplificador", true),
                item(2, "Bateria", true)
        );

        semImagem(1);
        semImagem(2);

        List<ItemResponse> respostas = service.montarRespostas(itens);

        assertEquals(2, respostas.size());
    }

    @Test
    @DisplayName("Deve montar lista vazia")
    void deveMontarListaVaziaSemConsultarImagens() {
        assertTrue(service.montarRespostas(List.of()).isEmpty());
    }

    @Test
    @DisplayName("Deve buscar item por id com sucesso")
    void deveBuscarItemPorId() {
        when(repository.findById(1))
                .thenReturn(Optional.of(item(1, "Amplificador", true)));
        semImagem(1);

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
        when(repository.findById(1))
                .thenReturn(Optional.of(item(1, "Amplificador", true)));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria()));
        when(unidadeRepository.findById(1)).thenReturn(Optional.of(unidade()));
        when(repository.save(any(Item.class)))
                .thenReturn(item(1, "Amplificador Novo", true));
        semImagem(1);

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
        when(repository.save(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        semImagem(1);

        ItemResponse resposta = service.update(
                1,
                request("Amplificador", 99)
        );

        assertEquals(7, resposta.quantidadeAtual());
        assertEquals(7, existente.getQuantidadeAtual());
        verify(movimentacaoService, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar item inexistente")
    void deveLancarExcecaoAoAtualizarItemInexistente() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> service.update(99, request("Amplificador"))
        );
    }

    @Test
    @DisplayName("Deve lançar EntityInativaException ao atualizar item inativo")
    void deveLancarExcecaoAoAtualizarItemInativo() {
        when(repository.findById(1))
                .thenReturn(Optional.of(item(1, "Amplificador", false)));

        assertThrows(
                EntityInativaException.class,
                () -> service.update(1, request("Amplificador"))
        );
    }

    @Test
    @DisplayName("Deve lançar EntityNomeJaExisteException ao atualizar para nome existente")
    void deveLancarExcecaoQuandoNomeJaExisteNaAtualizacao() {
        when(repository.findById(1))
                .thenReturn(Optional.of(item(1, "Amplificador", true)));
        when(repository.existsByNomeIgnoreCase("Outro Nome")).thenReturn(true);

        assertThrows(
                EntityNomeJaExisteException.class,
                () -> service.update(1, request("Outro Nome"))
        );
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

    @Test
    @DisplayName("Deve fazer upload de imagem com sucesso")
    void deveFazerUploadImagemComSucesso() {
        Item item = item(1, "Amplificador", true);

        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "foto.jpg",
                "image/jpeg",
                "conteudo".getBytes()
        );

        when(repository.findById(1)).thenReturn(Optional.of(item));

        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_ITEM,
                1,
                TIPO_ARQUIVO_IMAGEM
        )).thenReturn(Optional.empty());

        when(armazenamento.salvar(arquivo, "imagens"))
                .thenReturn("imagens/uuid-foto.jpg");

        when(arquivoRepository.save(any(Arquivo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_ITEM,
                1,
                TIPO_ARQUIVO_IMAGEM
        )).thenAnswer(invocation -> {
            Arquivo imagem = new Arquivo();
            imagem.setId(1);
            imagem.setTabelaOrigem(TABELA_ORIGEM_ITEM);
            imagem.setRegistroId(1);
            imagem.setTipoArquivo(TIPO_ARQUIVO_IMAGEM);
            imagem.setUri("imagens/uuid-foto.jpg");
            imagem.setNome("foto.jpg");
            imagem.setMimeType("image/jpeg");
            imagem.setTamanho(8);
            return Optional.of(imagem);
        });

        ItemResponse resposta = service.uploadImagem(1, arquivo);

        assertNotNull(resposta);
        assertEquals("Amplificador", resposta.nome());
        assertNotNull(resposta.imagem());
        assertEquals("/itens/1/imagem/download", resposta.imagem().url());
        assertEquals("foto.jpg", resposta.imagem().nomeArquivo());
        assertEquals("image/jpeg", resposta.imagem().mimeType());
        assertEquals(8, resposta.imagem().tamanhoBytes());

        verify(armazenamento).salvar(arquivo, "imagens");
        verify(arquivoRepository).save(any(Arquivo.class));
    }

    @Test
    @DisplayName("Deve deletar imagem antiga ao fazer upload de nova imagem")
    void deveDeletarImagemAntigaAoFazerUpload() {
        Item item = item(1, "Amplificador", true);
        Arquivo imagemAntiga = imagem(1, "imagens/uuid-antiga.jpg");

        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "foto.jpg",
                "image/jpeg",
                "conteudo".getBytes()
        );

        when(repository.findById(1)).thenReturn(Optional.of(item));
        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_ITEM,
                1,
                TIPO_ARQUIVO_IMAGEM
        )).thenReturn(Optional.of(imagemAntiga));
        when(armazenamento.salvar(arquivo, "imagens"))
                .thenReturn("imagens/uuid-nova.jpg");
        when(arquivoRepository.save(any(Arquivo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(armazenamento).deletar("imagens/uuid-antiga.jpg");

        service.uploadImagem(1, arquivo);

        verify(arquivoRepository).save(imagemAntiga);
        verify(armazenamento).deletar("imagens/uuid-antiga.jpg");
        assertEquals("imagens/uuid-nova.jpg", imagemAntiga.getUri());
    }

    @Test
    @DisplayName("Deve reverter upload se salvar metadados no banco falhar")
    void deveReverterUploadSeSalvarFalhar() {
        Item item = item(1, "Amplificador", true);

        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "foto.jpg",
                "image/jpeg",
                "conteudo".getBytes()
        );

        when(repository.findById(1)).thenReturn(Optional.of(item));
        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_ITEM,
                1,
                TIPO_ARQUIVO_IMAGEM
        )).thenReturn(Optional.empty());
        when(armazenamento.salvar(arquivo, "imagens"))
                .thenReturn("imagens/uuid-foto.jpg");
        when(arquivoRepository.save(any(Arquivo.class)))
                .thenThrow(new RuntimeException("Erro no banco"));
        doNothing().when(armazenamento).deletar("imagens/uuid-foto.jpg");

        assertThrows(
                RuntimeException.class,
                () -> service.uploadImagem(1, arquivo)
        );

        verify(armazenamento).deletar("imagens/uuid-foto.jpg");
    }

    @Test
    @DisplayName("Deve baixar imagem com sucesso")
    void deveBaixarImagemComSucesso() {
        Arquivo imagem = imagem(1, "imagens/uuid-foto.jpg");
        Resource recurso = new MockMultipartFile(
                "arquivo",
                "foto.jpg",
                "image/jpeg",
                "conteudo".getBytes()
        ).getResource();

        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_ITEM,
                1,
                TIPO_ARQUIVO_IMAGEM
        )).thenReturn(Optional.of(imagem));
        when(armazenamento.carregar("imagens/uuid-foto.jpg"))
                .thenReturn(recurso);

        Resource resultado = service.baixarImagem(1);

        assertNotNull(resultado);
        verify(armazenamento).carregar("imagens/uuid-foto.jpg");
    }

    @Test
    @DisplayName("Deve lançar exceção ao baixar imagem de item sem imagem")
    void deveLancarExcecaoAoBaixarImagemDeItemSemImagem() {
        semImagem(1);

        assertThrows(
                ArquivoInvalidoException.class,
                () -> service.baixarImagem(1)
        );
    }

    @Test
    @DisplayName("Deve deletar imagem com sucesso")
    void deveDeletarImagemComSucesso() {
        Arquivo imagem = imagem(1, "imagens/uuid-foto.jpg");

        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_ITEM,
                1,
                TIPO_ARQUIVO_IMAGEM
        )).thenReturn(Optional.of(imagem));
        doNothing().when(armazenamento).deletar("imagens/uuid-foto.jpg");

        service.deletarImagem(1);

        verify(arquivoRepository).delete(imagem);
        verify(armazenamento).deletar("imagens/uuid-foto.jpg");
    }

    @Test
    @DisplayName("Não deve tentar excluir arquivo no S3 quando item não possui imagem")
    void naoDeveExcluirArquivoNoS3QuandoItemNaoPossuiImagem() {
        semImagem(1);

        service.deletarImagem(1);

        verify(arquivoRepository, never()).delete(any(Arquivo.class));
        verify(armazenamento, never()).deletar(any(String.class));
    }
}