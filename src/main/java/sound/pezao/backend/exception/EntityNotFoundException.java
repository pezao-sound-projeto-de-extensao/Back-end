package sound.pezao.backend.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entidade, Integer id) {
        super(entidade + " não encontrado(a) com o id: " + id);
    }
}