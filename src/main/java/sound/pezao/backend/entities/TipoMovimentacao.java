package sound.pezao.backend.entities;

import java.util.Arrays;

/**
 * Tipos aceitos de movimentação de estoque.
 * O valor persistido é minúsculo por causa do check constraint da tabela
 * movimentacoes (tipo in ('entrada','saida')).
 */
public enum TipoMovimentacao {

    ENTRADA("entrada"),
    SAIDA("saida");

    private final String valor;

    TipoMovimentacao(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoMovimentacao fromValor(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Tipo de movimentação é obrigatório. Use 'entrada' ou 'saida'.");
        }
        return Arrays.stream(values())
                .filter(tipo -> tipo.valor.equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de movimentação inválido: '" + valor + "'. Use 'entrada' ou 'saida'."));
    }
}
