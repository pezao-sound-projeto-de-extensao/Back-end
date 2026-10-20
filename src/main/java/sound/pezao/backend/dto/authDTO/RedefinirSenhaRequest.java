package sound.pezao.backend.dto.authDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 5, max = 20) String senhaNova
) {
}
