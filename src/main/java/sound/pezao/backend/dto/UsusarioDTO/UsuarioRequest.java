package sound.pezao.backend.dto.UsusarioDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRequest (
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String senha
) {
}
