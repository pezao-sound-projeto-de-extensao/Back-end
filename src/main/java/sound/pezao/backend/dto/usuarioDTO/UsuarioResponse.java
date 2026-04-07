package sound.pezao.backend.dto.usuarioDTO;

import sound.pezao.backend.dto.cargoDTO.CargoResponse;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Integer id,
        String nome,
        String email,
        Boolean ativo,
        LocalDateTime ultimoAcesso,
        LocalDateTime criadoEm,
        CargoResponse cargo
) {
}
