package sound.pezao.backend.exception;

public class LoginInvalidoException extends RuntimeException {
    public LoginInvalidoException() {
        super("Email ou senha inválidos");
    }
}
