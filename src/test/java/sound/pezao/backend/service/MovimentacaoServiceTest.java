package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import sound.pezao.backend.entities.Arquivo;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.TipoMovimentacao;
import sound.pezao.backend.exception.ArquivoInvalidoException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ArquivoRepository;
import sound.pezao.backend.repository.MovimentacaoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para MovimentacaoService")
class MovimentacaoServiceTest {

    private static final String TABELA_ORIGEM_MOVIMENTACAO = "movimentacao";
    private static final String TIPO_ARQUIVO_NOTA_ENTRADA = "nota_entrada";

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private ArquivoRepository arquivoRepository;

    @Mock
    private ArmazenamentoArquivoService armazenamento;

    @InjectMocks
    private MovimentacaoService service;

    private Movimentacao movimentacao(Integer id) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId(id);
        movimentacao.setTipo(TipoMovimentacao.ENTRADA);
        movimentacao.setQuantidade(5);
        return movimentacao;
    }

    private Arquivo nota(Integer movimentacaoId, String uri) {
        Arquivo arquivo = new Arquivo();
        arquivo.setId(1);
        arquivo.setTabelaOrigem(TABELA_ORIGEM_MOVIMENTACAO);
        arquivo.setRegistroId(movimentacaoId);
        arquivo.setTipoArquivo(TIPO_ARQUIVO_NOTA_ENTRADA);
        arquivo.setUri(uri);
        arquivo.setNome("nota.pdf");
        arquivo.setMimeType("application/pdf");
        arquivo.setTamanho(8);
        arquivo.setCriadoEm(LocalDateTime.now());
        return arquivo;
    }

    private void semNota(Integer movimentacaoId) {
        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_MOVIMENTACAO,
                movimentacaoId,
                TIPO_ARQUIVO_NOTA_ENTRADA
        )).thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Deve salvar uma movimentação")
    void deveSalvarMovimentacao() {
        Movimentacao movimentacao = movimentacao(1);

        when(movimentacaoRepository.save(any(Movimentacao.class)))
                .thenReturn(movimentacao);

        Movimentacao resultado = service.salvar(movimentacao);

        assertEquals(1, resultado.getId());
    }

    @Test
    @DisplayName("Deve buscar movimentação por id com sucesso")
    void deveBuscarPorId() {
        when(movimentacaoRepository.findById(1))
                .thenReturn(Optional.of(movimentacao(1)));

        Movimentacao resultado = service.buscarPorId(1);

        assertEquals(1, resultado.getId());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao buscar movimentação inexistente")
    void deveLancarExcecaoQuandoMovimentacaoInexistente() {
        when(movimentacaoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.buscarPorId(99));
    }

    @Test
    @DisplayName("Deve listar movimentações com filtros")
    void deveListarComFiltros() {
        LocalDate inicio = LocalDate.now().minusDays(7);
        LocalDate fim = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 20);

        when(movimentacaoRepository.findWithFilters(
                1,
                TipoMovimentacao.ENTRADA,
                2,
                "cabo",
                inicio,
                fim,
                pageable
        )).thenReturn(new PageImpl<>(List.of(movimentacao(1))));

        Page<Movimentacao> resultado = service.listarComFiltros(
                1,
                TipoMovimentacao.ENTRADA,
                2,
                "cabo",
                inicio,
                fim,
                pageable
        );

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve deletar uma movimentação")
    void deveDeletarMovimentacao() {
        Movimentacao movimentacao = movimentacao(1);

        service.deletar(movimentacao);

        verify(movimentacaoRepository).delete(movimentacao);
    }

    @Test
    @DisplayName("Deve fazer upload de nota com sucesso")
    void deveFazerUploadNotaComSucesso() {
        Movimentacao movimentacao = movimentacao(1);

        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "nota.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        when(movimentacaoRepository.findById(1))
                .thenReturn(Optional.of(movimentacao));
        semNota(1);
        when(armazenamento.salvar(arquivo, "notas"))
                .thenReturn("notas/uuid-nota.pdf");
        when(arquivoRepository.save(any(Arquivo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Movimentacao resultado = service.uploadNota(1, arquivo);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());

        verify(armazenamento).salvar(arquivo, "notas");
        verify(arquivoRepository).save(any(Arquivo.class));
    }

    @Test
    @DisplayName("Deve deletar nota antiga ao fazer upload de nova nota")
    void deveDeletarNotaAntigaAoFazerUpload() {
        Movimentacao movimentacao = movimentacao(1);
        Arquivo notaAntiga = nota(1, "notas/uuid-antiga.pdf");

        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "nota.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        when(movimentacaoRepository.findById(1))
                .thenReturn(Optional.of(movimentacao));
        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_MOVIMENTACAO,
                1,
                TIPO_ARQUIVO_NOTA_ENTRADA
        )).thenReturn(Optional.of(notaAntiga));
        when(armazenamento.salvar(arquivo, "notas"))
                .thenReturn("notas/uuid-nova.pdf");
        when(arquivoRepository.save(any(Arquivo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(armazenamento).deletar("notas/uuid-antiga.pdf");

        service.uploadNota(1, arquivo);

        verify(arquivoRepository).save(notaAntiga);
        verify(armazenamento).deletar("notas/uuid-antiga.pdf");
        assertEquals("notas/uuid-nova.pdf", notaAntiga.getUri());
    }

    @Test
    @DisplayName("Deve reverter upload se salvar metadados no banco falhar")
    void deveReverterUploadSeSalvarFalhar() {
        Movimentacao movimentacao = movimentacao(1);

        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "nota.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        when(movimentacaoRepository.findById(1))
                .thenReturn(Optional.of(movimentacao));
        semNota(1);
        when(armazenamento.salvar(arquivo, "notas"))
                .thenReturn("notas/uuid-nota.pdf");
        when(arquivoRepository.save(any(Arquivo.class)))
                .thenThrow(new RuntimeException("Erro no banco"));
        doNothing().when(armazenamento).deletar("notas/uuid-nota.pdf");

        assertThrows(
                RuntimeException.class,
                () -> service.uploadNota(1, arquivo)
        );

        verify(armazenamento).deletar("notas/uuid-nota.pdf");
    }

    @Test
    @DisplayName("Deve baixar nota com sucesso")
    void deveBaixarNotaComSucesso() {
        Arquivo nota = nota(1, "notas/uuid-nota.pdf");

        Resource recurso = new MockMultipartFile(
                "arquivo",
                "nota.pdf",
                "application/pdf",
                "conteudo".getBytes()
        ).getResource();

        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_MOVIMENTACAO,
                1,
                TIPO_ARQUIVO_NOTA_ENTRADA
        )).thenReturn(Optional.of(nota));
        when(armazenamento.carregar("notas/uuid-nota.pdf"))
                .thenReturn(recurso);

        Resource resultado = service.baixarNota(1);

        assertNotNull(resultado);
        verify(armazenamento).carregar("notas/uuid-nota.pdf");
    }

    @Test
    @DisplayName("Deve lançar exceção ao baixar nota de movimentação sem nota")
    void deveLancarExcecaoAoBaixarNotaDeMovimentacaoSemNota() {
        semNota(1);

        assertThrows(
                ArquivoInvalidoException.class,
                () -> service.baixarNota(1)
        );
    }

    @Test
    @DisplayName("Deve deletar nota com sucesso")
    void deveDeletarNotaComSucesso() {
        Arquivo nota = nota(1, "notas/uuid-nota.pdf");

        when(arquivoRepository.findByTabelaOrigemAndRegistroIdAndTipoArquivo(
                TABELA_ORIGEM_MOVIMENTACAO,
                1,
                TIPO_ARQUIVO_NOTA_ENTRADA
        )).thenReturn(Optional.of(nota));
        doNothing().when(armazenamento).deletar("notas/uuid-nota.pdf");

        service.deletarNota(1);

        verify(arquivoRepository).delete(nota);
        verify(armazenamento).deletar("notas/uuid-nota.pdf");
    }

    @Test
    @DisplayName("Não deve tentar excluir arquivo no S3 quando movimentação não possui nota")
    void naoDeveExcluirArquivoNoS3QuandoMovimentacaoNaoPossuiNota() {
        semNota(1);

        service.deletarNota(1);

        verify(arquivoRepository, never()).delete(any(Arquivo.class));
        verify(armazenamento, never()).deletar(any(String.class));
        assertTrue(true);
    }
}