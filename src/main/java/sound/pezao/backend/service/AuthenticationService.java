package sound.pezao.backend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.authDTO.AuthRequest;
import sound.pezao.backend.dto.authDTO.AuthResponse;
import sound.pezao.backend.dto.authDTO.AuthTrocarSenhaRequest;
import sound.pezao.backend.dto.ususarioDTO.UsuarioMapper;
import sound.pezao.backend.entities.Usuario;
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

        boolean senhaInicial = passwordEncoder.matches(
                "PezaoSenha",
                usuario.getSenhaHash()
        );

        if (senhaInicial){
            throw new PrimeiroAcessoException("Altere a senha padrão antes de efetuar o login");
        }
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

        boolean senhaInicial = passwordEncoder.matches(
                "PezaoSenha",
                usuario.getSenhaHash()
        );

        if (!senhaInicial){
            throw new PrimeiroAcessoException("A senha já foi alterada uma vez");
        }
        boolean senhaCorreta = passwordEncoder.matches(
                authTrocarSenhaRequest.senhaAtual(),
                usuario.getSenhaHash()
        );

        if (!senhaCorreta){
            throw new LoginInvalidoException();
        }

        usuario.setSenhaHash(passwordEncoder.encode(authTrocarSenhaRequest.senhaNova()));
        usuarioRepository.save(usuario);

        System.out.println(passwordEncoder.matches(authTrocarSenhaRequest.senhaNova(), usuario.getSenhaHash()));

    }

}
