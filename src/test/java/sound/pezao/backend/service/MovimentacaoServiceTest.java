package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sound.pezao.backend.entities.Movimentacao;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.MovimentacaoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
}