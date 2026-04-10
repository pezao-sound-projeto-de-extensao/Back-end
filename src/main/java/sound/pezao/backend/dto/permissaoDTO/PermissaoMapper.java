package sound.pezao.backend.dto.permissaoDTO;

import sound.pezao.backend.entities.Permissao;

import java.util.List;

public class PermissaoMapper {
  public static PermissaoResponse toResponse(Permissao permissao){
    return new PermissaoResponse(
        permissao.getNome(),
        permissao.getDescricao()
    );
  }

  public static List<PermissaoResponse> toResponse(List<Permissao> permissoes){
    return permissoes.stream()
        .map(PermissaoMapper::toResponse)
        .toList();
  }

  public static Permissao toEntity(PermissaoRequest permissaoRequest){
    Permissao permissao = new Permissao();
    permissao.setNome(permissaoRequest.nome());
    permissao.setDescricao(permissaoRequest.descricao());
    return permissao;
  }
}
