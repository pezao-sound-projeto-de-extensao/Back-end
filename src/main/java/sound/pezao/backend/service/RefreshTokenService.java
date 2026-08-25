package sound.pezao.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sound.pezao.backend.entities.RefreshToken;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.repository.RefreshTokenRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String criar (Usuario usuario){
        String segredo = gerarSegredo();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash(passwordEncoder.encode(segredo));
        refreshToken.setExpiraEm(Instant.now().plusMillis(refreshExpirationMs));

        RefreshToken novo = refreshTokenRepository.save(refreshToken);

        return novo.getId() + "." + segredo;
    }

    @Transactional
    public Usuario validar(String token){
        String[] partes = token.split("\\.", 2);

        if (partes.length != 2){
            throw new IllegalArgumentException("Refesh token inválido");
        }
        Long tokenId;

        try {
            tokenId = Long.valueOf(partes[0]);
        } catch (Exception e){
            throw new IllegalArgumentException("Refresh token inválido");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByIdAndRevogadoEmIsNull(tokenId)
                .orElseThrow(() ->new IllegalArgumentException("Refresh token inválido ou revogado"));

        boolean segredoValido = passwordEncoder.matches(partes[1], refreshToken.getTokenHash());

        if (!segredoValido || !refreshToken.isAtvio()){
            throw new IllegalArgumentException("Refresh token inválido ou revogado");
        }
        refreshToken.setRevogadoEm(Instant.now());
        return refreshToken.getUsuario();
    }

    @Transactional
    public void revogar (String token){
        String[] partes = token.split("\\.", 2);

        if (partes.length != 2){
            return;
        }
        try {
            Long tokenId = Long.valueOf(partes[0]);

            refreshTokenRepository.findByIdAndRevogadoEmIsNull(tokenId)
                    .filter(t -> passwordEncoder.matches(partes[1], t.getTokenHash()))
                    .ifPresent(t -> t.setRevogadoEm(Instant.now()));

        } catch (NumberFormatException ignored){

        }
    }

    private String gerarSegredo(){
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
