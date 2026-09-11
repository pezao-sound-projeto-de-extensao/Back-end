package sound.pezao.backend.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Testes para StatusEstoque")
class StatusEstoqueTest {

    @Test
    @DisplayName("Deve marcar como zerado quando não há saldo")
    void deveMarcarComoZerado() {
        assertEquals(StatusEstoque.ZERADO, StatusEstoque.calcular(0, 2));
    }

    @Test
    @DisplayName("Deve marcar como baixo quando o saldo está no mínimo ou abaixo dele")
    void deveMarcarComoBaixo() {
        assertEquals(StatusEstoque.BAIXO, StatusEstoque.calcular(2, 2));
        assertEquals(StatusEstoque.BAIXO, StatusEstoque.calcular(1, 3));
    }

    @Test
    @DisplayName("Deve marcar como ok quando o saldo está acima do mínimo")
    void deveMarcarComoOk() {
        assertEquals(StatusEstoque.OK, StatusEstoque.calcular(15, 5));
    }

    @Test
    @DisplayName("Deve tratar mínimo zero com saldo disponível como ok")
    void deveTratarMinimoZeroComoOk() {
        assertEquals(StatusEstoque.OK, StatusEstoque.calcular(1, 0));
    }

    @Test
    @DisplayName("Deve tratar quantidades nulas como zero")
    void deveTratarNulosComoZero() {
        assertEquals(StatusEstoque.ZERADO, StatusEstoque.calcular(null, null));
        assertEquals(StatusEstoque.ZERADO, StatusEstoque.calcular(null, 5));
        assertEquals(StatusEstoque.OK, StatusEstoque.calcular(5, null));
    }
}
