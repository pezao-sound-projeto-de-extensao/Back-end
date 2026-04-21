package sound.pezao.backend.exception;

public class EntityNomeJaExisteException extends RuntimeException {
    public EntityNomeJaExisteException(String entidade, String nome) {
        super("Já existe um(a) " + entidade + " cadastrado(a) com o nome: " + nome);
    }
}