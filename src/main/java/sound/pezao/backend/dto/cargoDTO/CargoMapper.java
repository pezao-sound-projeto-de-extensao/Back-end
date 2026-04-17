package sound.pezao.backend.dto.cargoDTO;

import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Permissao;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CargoMapper {

    public static CargoResponse toResponse(Cargo cargo){
        Set<String> permissoes = cargo.getPermissoes()
            .stream()
            .map(Permissao::getNome)
            .collect(Collectors.toSet());

        return new CargoResponse(
            cargo.getId(),
            cargo.getNome(),
            cargo.getDescricao(),
            permissoes
        );
    }

    public static List<CargoResponse> toResponse(List<Cargo> cargos){
        return cargos.stream()
            .map(CargoMapper::toResponse)
            .toList();
    }

    public static Cargo toEntity(CargoRequest request, Set<Permissao> permissoes){
        Cargo cargo = new Cargo();
        cargo.setNome(request.nome());
        cargo.setDescricao(request.descricao());
        cargo.setPermissoes(permissoes);

        return cargo;
    }
}
