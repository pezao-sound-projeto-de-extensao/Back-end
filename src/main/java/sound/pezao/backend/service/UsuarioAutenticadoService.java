package sound.pezao.backend.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.LoginInvalidoException;
import sound.pezao.backend.repository.UsuarioRepository;

/**
 * Resolve o usuário dono da requisição atual, usado para registrar a autoria
 * das movimentações de estoque.
 */
@Service
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obter() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new LoginInvalidoException();
        }

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(LoginInvalidoException::new);
    }
}
