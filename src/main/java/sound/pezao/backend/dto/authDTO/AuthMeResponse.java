package sound.pezao.backend.dto.authDTO;

import sound.pezao.backend.dto.cargoDTO.CargoResponse;

public record AuthMeResponse(
        Integer id,
        String nome,
        String email,
        CargoResponse cargo
) {}
