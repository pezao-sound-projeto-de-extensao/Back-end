package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sound.pezao.backend.dto.authDTO.*;
import sound.pezao.backend.service.AuthenticationService;
import sound.pezao.backend.service.RecuperacaoSenhaService;
import sound.pezao.backend.service.UsuarioService;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Autenticação de aplicação")
public class AuthenticationController {

    final AuthenticationService authenticationService;
    private final UsuarioService usuarioService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;

    // o cookie vence junto com o JWT
    @Value("${jwt.expiration:300000}")
    private long jwtExpirationMs;

    public AuthenticationController(AuthenticationService authenticationService, UsuarioService usuarioService,
                                    RecuperacaoSenhaService recuperacaoSenhaService) {
        this.authenticationService = authenticationService;
        this.usuarioService = usuarioService;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
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
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refresh_token", required = false) String cookie,
                                                 @RequestBody(required = false) RefreshTokenRequest request,
                                                 HttpServletResponse response) {
        AuthResponse authResponse = authenticationService.refreshToken(refreshToken(cookie, request));
        setAuthCookies(response, authResponse.accessToken(), authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Revoga o refreshToken")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token", required = false) String cookie,
                                        @RequestBody(required = false) RefreshTokenRequest request,
                                        HttpServletResponse response) {
        authenticationService.logout(refreshToken(cookie, request));
        clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Trocar a senha informando a senha atual")
    @PostMapping("/trocar-senha")
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody AuthTrocarSenhaRequest authTrocarSenhaRequest) {
        authenticationService.trocarSenha(authTrocarSenhaRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Envia por e-mail um link para redefinir a senha",
            description = "Responde 202 mesmo quando o e-mail não está cadastrado, para não revelar quem tem conta. "
                    + "O e-mail é enviado em segundo plano pela fila.")
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Void> esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        recuperacaoSenhaService.solicitar(request.email());
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Define uma nova senha a partir do link recebido por e-mail")
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        recuperacaoSenhaService.redefinir(request.token(), request.senhaNova());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Invalida a senha do usuário e envia por e-mail o link para criar uma nova")
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

    // O front usa o cookie HttpOnly; o corpo continua aceito para clientes como o Bruno
    private String refreshToken(String cookie, RefreshTokenRequest request) {
        if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
            return request.refreshToken();
        }
        return cookie;
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(false)        // obrigatório false em HTTP
                .sameSite("Lax")      // não pode ser "None" sem HTTPS
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpirationMs))
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
