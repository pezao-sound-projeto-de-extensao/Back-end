package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sound.pezao.backend.dto.authDTO.*;
import sound.pezao.backend.service.AuthenticationService;
import sound.pezao.backend.service.UsuarioService;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Autenticação de aplicação")
public class AuthenticationController {

    final AuthenticationService authenticationService;
    private final UsuarioService usuarioService;

    public AuthenticationController(AuthenticationService authenticationService, UsuarioService usuarioService) {
        this.authenticationService = authenticationService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Login e geração do accessToken e refreshToken.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest,
                                               HttpServletResponse response) {
        AuthResponse authResponse = authenticationService.authenticate(authRequest);
        setAuthCookies(response, authResponse.accessToken(), authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Gera novo accessToken e refreshToken.")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                                 HttpServletResponse response) {
        AuthResponse authResponse = authenticationService.refreshToken(request.refreshToken());
        setAuthCookies(response, authResponse.accessToken(), authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Revoga o refreshToken")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request,
                                        HttpServletResponse response) {
        authenticationService.logout(request.refreshToken());
        clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Trocar senha no primeiro acesso")
    @PostMapping("/trocar-senha")
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody AuthTrocarSenhaRequest authTrocarSenhaRequest) {
        authenticationService.trocarSenha(authTrocarSenhaRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Resetar a senha do usuário para a padrão")
    @PutMapping("/resetar-senha/{id}")
    public ResponseEntity<Void> resetarSenha(@PathVariable int id) {
        authenticationService.resetarSenha(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Retorna dados do usuário autenticado")
    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        AuthMeResponse response = usuarioService.buscarMePorEmail(email);
        return ResponseEntity.ok(response);
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(false)        // obrigatório false em HTTP
                .sameSite("Lax")      // não pode ser "None" sem HTTPS
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
