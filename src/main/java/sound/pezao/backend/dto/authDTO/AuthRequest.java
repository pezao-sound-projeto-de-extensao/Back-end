package sound.pezao.backend.dto.authDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotNull @Email String email,
        @NotNull @Size (min = 5, max = 20) String senha
) {
}
