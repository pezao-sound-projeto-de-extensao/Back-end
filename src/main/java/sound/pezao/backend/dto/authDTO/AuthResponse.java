package sound.pezao.backend.dto.authDTO;

import sound.pezao.backend.dto.ususarioDTO.UsuarioResponse;

public record AuthResponse(
        String toekn,
        String username,
        UsuarioResponse usuario
) {
}
