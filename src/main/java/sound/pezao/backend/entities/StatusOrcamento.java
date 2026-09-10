package sound.pezao.backend.entities;

import java.util.Arrays;

/**
 * Situação de um orçamento. As transições possíveis são:
 * PENDENTE -> ACEITO ou REJEITADO, e ACEITO -> CONCLUIDO, quando todas as
 * encomendas geradas pelo aceite forem entregues.
 */
public enum StatusOrcamento {

    PENDENTE,
    ACEITO,
    REJEITADO,
    CONCLUIDO;

    public static StatusOrcamento fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Status de orçamento inválido: '" + valor
                                + "'. Use PENDENTE, ACEITO, REJEITADO ou CONCLUIDO."));
    }

    public boolean permiteEdicao() {
        return this == PENDENTE;
    }
}
