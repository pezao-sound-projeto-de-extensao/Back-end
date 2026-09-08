package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.exception.ArquivoInvalidoException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.MovimentacaoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para MovimentacaoService")
class MovimentacaoServiceTest {

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private ArmazenamentoArquivoService armazenamento;

    @InjectMocks
    private MovimentacaoService service;

    private Movimentacao movimentacao(Integer id) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId(id);
        movimentacao.setTipo("entrada");
        movimentacao.setQuantidade(5);
        return movimentacao;
    }

    @Test
    @DisplayName("Deve salvar uma movimentação")
    void deveSalvarMovimentacao() {
        Movimentacao movimentacao = movimentacao(1);
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenReturn(movimentacao);

        Movimentacao resultado = service.salvar(movimentacao);

        assertEquals(1, resultado.getId());
    }

    @Test
    @DisplayName("Deve buscar movimentação por id com sucesso")
    void deveBuscarPorId() {
        when(movimentacaoRepository.findById(1)).thenReturn(Optional.of(movimentacao(1)));

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
        LocalDateTime inicio = LocalDateTime.now().minusDays(7);
        LocalDateTime fim = LocalDateTime.now();
        when(movimentacaoRepository.findWithFilters(1, "entrada", 2, inicio, fim))
                .thenReturn(List.of(movimentacao(1)));

        List<Movimentacao> resultado = service.listarComFiltros(1, "entrada", 2, inicio, fim);

        assertEquals(1, resultado.size());
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
    void deveFazerUploadNotaComSucesso() throws Exception {
        Movimentacao movimentacao = movimentacao(1);
        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "nota.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        when(movimentacaoRepository.findById(1)).thenReturn(Optional.of(movimentacao));
        when(armazenamento.salvar(arquivo, "notas")).thenReturn("notas/uuid-nota.pdf");
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Movimentacao resultado = service.uploadNota(1, arquivo);

        assertNotNull(resultado);
        assertEquals("notas/uuid-nota.pdf", resultado.getUriNotaEntrada());
        verify(movimentacaoRepository).save(movimentacao);
        verify(armazenamento).salvar(arquivo, "notas");
    }

    @Test
    @DisplayName("Deve deletar nota antiga ao fazer upload de nova nota")
    void deveDeletarNotaAntigaAoFazerUpload() throws Exception {
        Movimentacao movimentacao = movimentacao(1);
        movimentacao.setUriNotaEntrada("notas/uuid-antiga.pdf");
        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "nota.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        when(movimentacaoRepository.findById(1)).thenReturn(Optional.of(movimentacao));
        when(armazenamento.salvar(arquivo, "notas")).thenReturn("notas/uuid-nova.pdf");
        doNothing().when(armazenamento).deletar("notas/uuid-antiga.pdf");
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.uploadNota(1, arquivo);

        verify(armazenamento).deletar("notas/uuid-antiga.pdf");
        assertEquals("notas/uuid-nova.pdf", movimentacao.getUriNotaEntrada());
    }

    @Test
    @DisplayName("Deve reverter upload se salvar no banco falhar")
    void deveReverterUploadSeSalvarFalhar() throws Exception {
        Movimentacao movimentacao = movimentacao(1);
        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "nota.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        when(movimentacaoRepository.findById(1)).thenReturn(Optional.of(movimentacao));
        when(armazenamento.salvar(arquivo, "notas")).thenReturn("notas/uuid-nota.pdf");
        doNothing().when(armazenamento).deletar("notas/uuid-nota.pdf");
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenThrow(new RuntimeException("Erro no banco"));

        assertThrows(RuntimeException.class, () -> service.uploadNota(1, arquivo));

        verify(armazenamento).deletar("notas/uuid-nota.pdf");
    }

    @Test
    @DisplayName("Deve baixar nota com sucesso")
    void deveBaixarNotaComSucesso() {
        Movimentacao movimentacao = movimentacao(1);
        movimentacao.setUriNotaEntrada("notas/uuid-nota.pdf");
        Resource recurso = new MockMultipartFile("arquivo", "nota.pdf", "application/pdf", "conteudo".getBytes()).getResource();

        when(movimentacaoRepository.findById(1)).thenReturn(Optional.of(movimentacao));
        when(armazenamento.carregar("notas/uuid-nota.pdf")).thenReturn(recurso);

        Resource resultado = service.baixarNota(1);

        assertNotNull(resultado);
        verify(armazenamento).carregar("notas/uuid-nota.pdf");
    }

    @Test
    @DisplayName("Deve lançar exceção ao baixar nota de movimentação sem nota")
    void deveLancarExcecaoAoBaixarNotaDeMovimentacaoSemNota() {
        Movimentacao movimentacao = movimentacao(1);

        when(movimentacaoRepository.findById(1)).thenReturn(Optional.of(movimentacao));

        assertThrows(ArquivoInvalidoException.class, () -> service.baixarNota(1));
    }

    @Test
    @DisplayName("Deve deletar nota com sucesso")
    void deveDeletarNotaComSucesso() {
        Movimentacao movimentacao = movimentacao(1);
        movimentacao.setUriNotaEntrada("notas/uuid-nota.pdf");

        when(movimentacaoRepository.findById(1)).thenReturn(Optional.of(movimentacao));
        doNothing().when(armazenamento).deletar("notas/uuid-nota.pdf");
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deletarNota(1);

        verify(movimentacaoRepository).save(movimentacao);
        verify(armazenamento).deletar("notas/uuid-nota.pdf");
        assertNull(movimentacao.getUriNotaEntrada());
        assertNull(movimentacao.getNomeNotaEntrada());
        assertNull(movimentacao.getMimeTypeNotaEntrada());
        assertNull(movimentacao.getTamanhoNotaEntrada());
    }

    @Test
    @DisplayName("Deve deletar nota do banco mesmo se S3 falhar")
    void deveDeletarNotaDoBancoMesmoSeS3Falhar() {
        Movimentacao movimentacao = movimentacao(1);
        movimentacao.setUriNotaEntrada("notas/uuid-nota.pdf");

        when(movimentacaoRepository.findById(1)).thenReturn(Optional.of(movimentacao));
        doNothing().when(armazenamento).deletar("notas/uuid-nota.pdf");
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deletarNota(1);

        verify(movimentacaoRepository).save(movimentacao);
        assertNull(movimentacao.getUriNotaEntrada());
    }
}