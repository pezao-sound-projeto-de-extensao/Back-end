package sound.pezao.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sound.pezao.backend.dto.authDTO.AuthRequest;
import sound.pezao.backend.dto.authDTO.AuthResponse;
import sound.pezao.backend.dto.authDTO.AuthTrocarSenhaRequest;
import sound.pezao.backend.service.AuthenticationService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Autenticação de aplicação")
public class AuthenticationController {

    final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Login e geração do token JWT.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@Valid  @RequestBody AuthRequest authRequest){
        return ResponseEntity.ok(authenticationService.authenticate(authRequest));
    }

    @Operation(summary = "Trocar senha no primeiro acesso")
    @PostMapping("/trocar-senha")
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody AuthTrocarSenhaRequest authTrocarSenhaRequest){
        authenticationService.trocarSenha(authTrocarSenhaRequest);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Resetar a senha do usuário para a padrão")
    @PutMapping("/resetar-senha/{id}")
    public ResponseEntity<Void> resetarSenha (@PathVariable int id){
        authenticationService.resetarSenha(id);
        return ResponseEntity.noContent().build();
    }
}
