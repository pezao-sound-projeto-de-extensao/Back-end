package sound.pezao.backend.exception;

public class EntityInativaException extends RuntimeException {
    public EntityInativaException(String entidade, Integer id) {
        super(entidade + " com id " + id + " está inativo(a) e não pode ser alterado(a)");
    }
}