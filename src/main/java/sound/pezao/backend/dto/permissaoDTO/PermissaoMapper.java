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

  public static Permissao toEntity(PermissaoRequest request){
    Permissao permissao = new Permissao();
    permissao.setNome(request.nome());
    permissao.setDescricao(request.descricao());
    return permissao;
  }
}
