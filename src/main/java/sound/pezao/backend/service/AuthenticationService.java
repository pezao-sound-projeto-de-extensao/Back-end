package sound.pezao.backend.service;

import org.springframework.security.core.Authentication;
import sound.pezao.backend.security.JwtService;

public class AuthenticationService {
    private final JwtService jwtService;

    public AuthenticationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String authenticate(Authentication authentication){
        return jwtService.generateToken(authentication);
    }

}
