package sound.pezao.backend.exception;

public class EstoqueInsuficienteException extends RuntimeException {
    public EstoqueInsuficienteException(Integer estoqueAntes, Integer quantidade) {
        super("Estoque insuficiente. Atual: " + estoqueAntes + ", solicitado: " + quantidade);
    }
}
