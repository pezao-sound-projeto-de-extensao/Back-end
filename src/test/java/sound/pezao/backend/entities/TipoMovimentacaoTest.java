package sound.pezao.backend.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Testes para TipoMovimentacao")
class TipoMovimentacaoTest {

    @Test
    @DisplayName("Deve converter os valores aceitos ignorando caixa e espaços")
    void deveConverterValoresAceitos() {
        assertEquals(TipoMovimentacao.ENTRADA, TipoMovimentacao.fromValor("entrada"));
        assertEquals(TipoMovimentacao.ENTRADA, TipoMovimentacao.fromValor("ENTRADA"));
        assertEquals(TipoMovimentacao.SAIDA, TipoMovimentacao.fromValor("Saida"));
        assertEquals(TipoMovimentacao.SAIDA, TipoMovimentacao.fromValor("  saida  "));
    }

    @Test
    @DisplayName("Deve persistir os valores em minúsculo, como espera o check constraint")
    void devePersistirValoresEmMinusculo() {
        assertEquals("entrada", TipoMovimentacao.ENTRADA.getValor());
        assertEquals("saida", TipoMovimentacao.SAIDA.getValor());
    }

    @Test
    @DisplayName("Deve recusar tipo desconhecido em vez de tratar como entrada")
    void deveRecusarTipoDesconhecido() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> TipoMovimentacao.fromValor("transferencia"));

        assertEquals("Tipo de movimentação inválido: 'transferencia'. Use 'entrada' ou 'saida'.",
                erro.getMessage());
    }

    @Test
    @DisplayName("Deve recusar tipo nulo")
    void deveRecusarTipoNulo() {
        assertThrows(IllegalArgumentException.class, () -> TipoMovimentacao.fromValor(null));
    }
}
