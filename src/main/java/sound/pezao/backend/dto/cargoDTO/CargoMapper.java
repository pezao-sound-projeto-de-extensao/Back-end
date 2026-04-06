package sound.pezao.backend.dto.cargoDTO;

import sound.pezao.backend.entities.Cargo;

public class CargoMapper {

    public static CargoResponse toResponse(Cargo cargo){
        return new CargoResponse(
                cargo.getId(),
                cargo.getNome(),
                cargo.getDescricao()
        );
    }
}
