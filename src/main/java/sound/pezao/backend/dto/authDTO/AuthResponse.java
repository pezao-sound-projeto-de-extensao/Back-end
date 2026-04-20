package sound.pezao.backend.dto.authDTO;

import sound.pezao.backend.dto.usuarioDTO.UsuarioResponse;

public record AuthResponse(
        String token,
        String username,
        UsuarioResponse usuario
) {
}
