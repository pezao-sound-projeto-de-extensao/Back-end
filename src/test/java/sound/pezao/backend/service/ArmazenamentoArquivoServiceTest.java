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
import sound.pezao.backend.exception.ArquivoInvalidoException;
import sound.pezao.backend.service.storage.ArmazenamentoArquivoStrategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ArmazenamentoArquivoService")
class ArmazenamentoArquivoServiceTest {

    @Mock
    private ArmazenamentoArquivoStrategy strategy;

    @InjectMocks
    private ArmazenamentoArquivoService service;

    @Test
    @DisplayName("Deve delegar o salvamento para a strategy")
    void deveDelegarSalvamentoParaStrategy() {
        MultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "foto.jpg",
                "image/jpeg",
                "conteudo".getBytes()
        );

        when(strategy.salvar(arquivo, "imagens"))
                .thenReturn("imagens/uuid.jpg");

        String resultado = service.salvar(arquivo, "imagens");

        assertEquals("imagens/uuid.jpg", resultado);
        verify(strategy).salvar(arquivo, "imagens");
    }

    @Test
    @DisplayName("Deve delegar o carregamento para a strategy")
    void deveDelegarCarregamentoParaStrategy() {
        Resource recurso = mock(Resource.class);

        when(strategy.carregar("imagens/uuid.jpg"))
                .thenReturn(recurso);

        Resource resultado = service.carregar("imagens/uuid.jpg");

        assertSame(recurso, resultado);
        verify(strategy).carregar("imagens/uuid.jpg");
    }

    @Test
    @DisplayName("Deve delegar a exclusão para a strategy")
    void deveDelegarExclusaoParaStrategy() {
        service.deletar("imagens/uuid.jpg");

        verify(strategy).deletar("imagens/uuid.jpg");
    }
}