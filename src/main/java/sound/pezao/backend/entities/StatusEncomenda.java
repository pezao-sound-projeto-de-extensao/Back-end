package sound.pezao.backend.entities;

import java.util.Arrays;

/**
 * Etapas de uma encomenda gerada pelo aceite de um orçamento:
 * PENDENTE aguarda a chegada do fornecedor, RECEBIDA já deu entrada no estoque
 * e CONCLUIDA foi entregue ao cliente, com a saída registrada.
 */
public enum StatusEncomenda {

    PENDENTE,
    RECEBIDA,
    CONCLUIDA;

    public static StatusEncomenda fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Status de encomenda inválido: '" + valor
                                + "'. Use PENDENTE, RECEBIDA ou CONCLUIDA."));
    }
}
