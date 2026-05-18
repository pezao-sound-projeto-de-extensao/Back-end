package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import sound.pezao.backend.dto.arquivoDTO.ArquivoDownload;
import sound.pezao.backend.dto.notaEntradaDTO.NotaEntradaResponse;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.entities.NotaEntrada;
import sound.pezao.backend.exception.ArquivoInvalidoException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.MovimentacaoRepository;
import sound.pezao.backend.repository.NotaEntradaRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para NotaEntradaService")
class NotaEntradaServiceTest {

    @Mock
    private NotaEntradaRepository repository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private ArmazenamentoArquivoService armazenamento;

    @InjectMocks
    private NotaEntradaService service;

    private final MultipartFile arquivo =
            new MockMultipartFile("arquivo", "nf.pdf", "application/pdf", "conteudo".getBytes());

    private Movimentacao movimentacao(Integer id) {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId(id);
        return movimentacao;
    }

    private NotaEntrada nota(Integer id, Movimentacao movimentacao) {
        NotaEntrada nota = new NotaEntrada();
        nota.setId(id);
        nota.setMovimentacao(movimentacao);
        nota.setTipo("nota_fiscal");
        nota.setNomeArquivo("nf.pdf");
        nota.setCaminho("notas-entrada/uuid.pdf");
        nota.setMimeType("application/pdf");
        return nota;
    }

    @Test
    @DisplayName("Deve fazer upload de nota com sucesso")
    void deveFazerUploadComSucesso() {
        Movimentacao movimentacao = movimentacao(1);
        when(movimentacaoRepository.findById(1)).thenReturn(Optional.of(movimentacao));
        when(armazenamento.salvar(any(), eq("notas-entrada"))).thenReturn("notas-entrada/uuid.pdf");
        when(repository.save(any(NotaEntrada.class))).thenReturn(nota(7, movimentacao));

        NotaEntradaResponse resposta = service.upload(1, arquivo, "nota_fiscal");

        assertNotNull(resposta);
        assertEquals("nota_fiscal", resposta.tipo());
    }

    @Test
    @DisplayName("Deve lançar ArquivoInvalidoException quando o tipo é inválido")
    void deveLancarExcecaoQuandoTipoInvalido() {
        assertThrows(ArquivoInvalidoException.class,
                () -> service.upload(1, arquivo, "documento"));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao subir nota para movimentação inexistente")
    void deveLancarExcecaoQuandoMovimentacaoInexistente() {
        when(movimentacaoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.upload(99, arquivo, "imagem"));
    }

    @Test
    @DisplayName("Deve listar as notas de uma movimentação")
    void deveListarNotasDaMovimentacao() {
        when(movimentacaoRepository.existsById(1)).thenReturn(true);
        when(repository.findByMovimentacao_Id(1)).thenReturn(List.of(nota(7, movimentacao(1))));

        List<NotaEntradaResponse> resultado = service.listar(1);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Deve retornar o arquivo da nota para download")
    void deveBaixarNota() {
        NotaEntrada nota = nota(7, movimentacao(1));
        Resource recurso = new ByteArrayResource("conteudo".getBytes());
        when(repository.findById(7)).thenReturn(Optional.of(nota));
        when(armazenamento.carregar("notas-entrada/uuid.pdf")).thenReturn(recurso);

        ArquivoDownload download = service.baixar(1, 7);

        assertEquals("nf.pdf", download.nomeArquivo());
        assertEquals(recurso, download.conteudo());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando a nota não pertence à movimentação")
    void deveLancarExcecaoQuandoNotaNaoPertenceAMovimentacao() {
        when(repository.findById(7)).thenReturn(Optional.of(nota(7, movimentacao(2))));

        assertThrows(EntityNotFoundException.class, () -> service.baixar(1, 7));
    }

    @Test
    @DisplayName("Deve remover a nota e o arquivo do storage")
    void deveDeletarNota() {
        NotaEntrada nota = nota(7, movimentacao(1));
        when(repository.findById(7)).thenReturn(Optional.of(nota));

        service.deletar(1, 7);

        verify(repository).delete(nota);
        verify(armazenamento).deletar("notas-entrada/uuid.pdf");
    }
}
