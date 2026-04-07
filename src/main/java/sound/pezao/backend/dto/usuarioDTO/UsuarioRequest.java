package sound.pezao.backend.dto.usuarioDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UsuarioRequest (
        @NotBlank String nome,
        @NotBlank @Email String email,
        @Positive int cargo_id
) {
}
