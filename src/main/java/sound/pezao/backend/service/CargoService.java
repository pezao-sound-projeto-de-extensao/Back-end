package sound.pezao.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.cargoDTO.CargoMapper;
import sound.pezao.backend.dto.cargoDTO.CargoRequest;
import sound.pezao.backend.dto.cargoDTO.CargoResponse;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Permissao;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.repository.CargoRepository;
import sound.pezao.backend.repository.PermissaoRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PreAuthorize("hasAuthority('GERENCIAR_CARGOS')")
@Service
public class CargoService {
  private final CargoRepository cargoRepository;
  private final PermissaoRepository permissaoRepository;

  public CargoService(CargoRepository cargoRepository,
                      PermissaoRepository permissaoRepository) {
    this.cargoRepository = cargoRepository;
    this.permissaoRepository = permissaoRepository;
  }

  public List<CargoResponse> findAll() {
    return cargoRepository.findAll()
        .stream()
        .map(CargoMapper::toResponse)
        .toList();
  }

  public CargoResponse findById(Integer id) {
    Cargo cargo = cargoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Cargo não encontrado"));

    return CargoMapper.toResponse(cargo);
  }

  @Transactional
  public CargoResponse create(CargoRequest request) {
    if (cargoRepository.existsByNomeIgnoreCase((request.nome()))) {
      throw new EntityNomeJaExisteException("Cargo", request.nome());
    }
    Cargo cargo = new Cargo();
    cargo.setNome(request.nome());
    cargo.setDescricao(request.descricao());

    cargo.setPermissoes(buscarPermissoes(request.permissoes()));

    return CargoMapper.toResponse(cargoRepository.save(cargo));
  }

  @Transactional
  public CargoResponse update(Integer id, CargoRequest request) {
    Cargo cargo = cargoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Cargo não encontrado"));

    if (!cargo.getNome().equalsIgnoreCase(request.nome())
        && cargoRepository.existsByNomeIgnoreCase(request.nome())) {
      throw new EntityNomeJaExisteException("Cargo", request.nome());
    }

    cargo.setNome(request.nome());
    cargo.setDescricao(request.descricao());
    cargo.setPermissoes(buscarPermissoes(request.permissoes()));

    return CargoMapper.toResponse(cargoRepository.save(cargo));
  }

  public void delete(Integer id) {
    cargoRepository.deleteById(id);
  }

  private Set<Permissao> buscarPermissoes(Set<String> nomes) {
    List<Permissao> permissoes = permissaoRepository.findByNomeIn(nomes);

    if (permissoes.size() != nomes.size()) {
      throw new RuntimeException("Alguma permissão não existe");
    }

    return permissoes.stream().collect(Collectors.toSet());
  }

}
