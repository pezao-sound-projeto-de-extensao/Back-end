package sound.pezao.backend.exception;

public class EntityEmUsoException extends RuntimeException {
    public EntityEmUsoException(String entidade, Integer id) {
        super(entidade + " com id " + id + " está em uso e não pode ser removido(a)");
    }
}
