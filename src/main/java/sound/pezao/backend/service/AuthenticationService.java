package sound.pezao.backend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.authDTO.AuthRequest;
import sound.pezao.backend.dto.authDTO.AuthResponse;
import sound.pezao.backend.dto.authDTO.AuthTrocarSenhaRequest;
import sound.pezao.backend.dto.usuarioDTO.UsuarioMapper;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.exception.LoginInvalidoException;
import sound.pezao.backend.exception.PrimeiroAcessoException;
import sound.pezao.backend.repository.UsuarioRepository;
import sound.pezao.backend.security.JwtService;
import sound.pezao.backend.security.UserAuthenticated;

import java.time.LocalDateTime;

@Service
public class AuthenticationService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(JwtService jwtService, AuthenticationManager authenticationManager,
                                 UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse authenticate(AuthRequest authRequest){
        Usuario usuario = usuarioRepository.findByEmail(authRequest.email())
                .orElseThrow(LoginInvalidoException::new);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.email(),
                        authRequest.senha()
                )
        );
        usuario.setUltimoAcesso(LocalDateTime.now());
        usuarioRepository.save(usuario);
        String token = jwtService.generateToken(authentication);
        UserAuthenticated userDetails = (UserAuthenticated) authentication.getPrincipal();
        return new AuthResponse(token, userDetails.getUsername(), UsuarioMapper.toResponse(usuario));
    }

    public void trocarSenha(AuthTrocarSenhaRequest authTrocarSenhaRequest){
        Usuario usuario = usuarioRepository.findByEmail(authTrocarSenhaRequest.email())
                .orElseThrow(LoginInvalidoException::new);

        boolean senhaCorreta = passwordEncoder.matches(
                authTrocarSenhaRequest.senhaAtual(),
                usuario.getSenhaHash()
        );

        if (!senhaCorreta){
            throw new LoginInvalidoException();
        }

        usuario.setSenhaHash(passwordEncoder.encode(authTrocarSenhaRequest.senhaNova()));
        usuarioRepository.save(usuario);
    }

    @PreAuthorize("hasAuthority('GERENCIAR_USUARIOS')")
    public void resetarSenha(int id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));

        String senhaPadrao = gerarSenhaPadraoDoUsuario(usuario.getId());
        usuario.setSenhaHash(passwordEncoder.encode(senhaPadrao));
        usuarioRepository.save(usuario);
    }

    private String gerarSenhaPadraoDoUsuario(Integer usuarioId) {
        // Essa função retorna uma senha padrão única por usuário, diferente ao que definimos antes de ser uma senha IGUAL para todos, ex: Pezao_0001, Pezao_0042, etc.
        return "Pezao_" + String.format("%04d", usuarioId);
    }
}
