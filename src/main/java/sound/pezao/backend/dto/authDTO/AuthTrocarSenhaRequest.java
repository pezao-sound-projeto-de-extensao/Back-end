package sound.pezao.backend.dto.authDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthTrocarSenhaRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 5, max = 20) String senhaAtual,
        @NotBlank @Size (min = 5, max = 20) String senhaNova
) {
}
