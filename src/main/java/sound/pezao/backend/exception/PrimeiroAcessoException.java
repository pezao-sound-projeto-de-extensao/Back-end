package sound.pezao.backend.exception;

public class PrimeiroAcessoException extends RuntimeException {
    public PrimeiroAcessoException() {
        super("Altere a senha padrão antes de efetuar o login");
    }
}
