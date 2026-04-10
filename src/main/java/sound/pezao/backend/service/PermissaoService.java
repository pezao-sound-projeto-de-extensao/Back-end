package sound.pezao.backend.service;

import sound.pezao.backend.dto.permissaoDTO.PermissaoMapper;
import sound.pezao.backend.dto.permissaoDTO.PermissaoResponse;
import sound.pezao.backend.repository.PermissaoRepository;

import java.util.List;

public class PermissaoService {
  private final PermissaoRepository repository;

  public PermissaoService(PermissaoRepository repository) {
    this.repository = repository;
  }

  public List<PermissaoResponse> findAll(){
    return PermissaoMapper.toResponse(repository.findAll());
  }
}
