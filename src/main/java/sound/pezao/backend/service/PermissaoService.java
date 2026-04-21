package sound.pezao.backend.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.permissaoDTO.PermissaoMapper;
import sound.pezao.backend.dto.permissaoDTO.PermissaoResponse;
import sound.pezao.backend.repository.PermissaoRepository;

import java.util.List;

@PreAuthorize("hasAuthority('GERENCIAR_CARGOS')")
@Service
public class PermissaoService {
  private final PermissaoRepository repository;

  public PermissaoService(PermissaoRepository repository) {
    this.repository = repository;
  }

  public List<PermissaoResponse> findAll(){
    return PermissaoMapper.toResponse(repository.findAll());
  }
}
