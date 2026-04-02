package sound.pezao.backend.dto.ususarioDTO;

import sound.pezao.backend.dto.cargoDTO.CargoMapper;
import sound.pezao.backend.entities.Usuario;

public class UsuarioMapper {

    public static Usuario toEntity(UsuarioRequest usuarioRequest) {

        return Usuario.builder()
                .nome(usuarioRequest.nome())
                .email(usuarioRequest.email())
                .build();
    }

    public static UsuarioResponse toResponse(Usuario usuario){
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.isAtivo(),
                usuario.getUltimoAcesso(),
                usuario.getCriadoEm(),
                CargoMapper.toResponse(usuario.getCargo())

        );
    }
}
