package sound.pezao.backend.entities;

/**
 * Situação do saldo de um item, derivada da quantidade atual versus a mínima.
 * Não é persistida: é sempre calculada, para não existir a possibilidade de o
 * status gravado divergir do estoque real.
 */
public enum StatusEstoque {

    OK,
    BAIXO,
    ZERADO;

    public static StatusEstoque calcular(Integer quantidadeAtual, Integer quantidadeMinima) {
        int atual = quantidadeAtual != null ? quantidadeAtual : 0;
        int minima = quantidadeMinima != null ? quantidadeMinima : 0;

        if (atual <= 0) {
            return ZERADO;
        }
        return atual <= minima ? BAIXO : OK;
    }
}
