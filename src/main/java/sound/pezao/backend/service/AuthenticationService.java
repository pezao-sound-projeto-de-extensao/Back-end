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

import java.security.SecureRandom;
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

        // Nota: senha agora é sempre aleatória, sem necessidade de verificar senha padrão
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
        String senhaAleatoria = gerarSenhaAleatoria();
        usuario.setSenhaHash(passwordEncoder.encode(senhaAleatoria));
        usuarioRepository.save(usuario);
    }

    private String gerarSenhaAleatoria() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder senha = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            senha.append(chars.charAt(random.nextInt(chars.length())));
        }
        return senha.toString();
    }
}
