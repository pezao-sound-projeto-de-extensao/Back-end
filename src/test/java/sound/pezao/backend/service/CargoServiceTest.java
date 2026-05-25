package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sound.pezao.backend.dto.cargoDTO.CargoMapper;
import sound.pezao.backend.dto.cargoDTO.CargoRequest;
import sound.pezao.backend.dto.cargoDTO.CargoResponse;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Permissao;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.repository.CargoRepository;
import sound.pezao.backend.repository.PermissaoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CargoService")
class CargoServiceTest {

  @Mock
  private CargoRepository cargoRepository;

  @Mock
  private PermissaoRepository permissaoRepository;

  @InjectMocks
  private CargoService service;

  private Permissao permissao(String nome) {
    Permissao permissao = new Permissao();
    permissao.setNome(nome);
    permissao.setDescricao("Descrição de " + nome);
    return permissao;
  }

  private Cargo cargo(Integer id, String nome) {
    Cargo cargo = new Cargo();
    cargo.setId(id);
    cargo.setNome(nome);
    cargo.setDescricao("Descrição de " + nome);
    cargo.setPermissoes(Set.of(permissao("cadastrar_itens")));
    return cargo;
  }

  private CargoRequest request(String nome) {
    return new CargoRequest(nome, "Descrição de " + nome, Set.of("cadastrar_itens"));
  }

  private CargoResponse response(Integer id, String nome) {
    return new CargoResponse(id, nome, "Descrição de " + nome, Set.of("cadastrar_itens"));
  }

  @Test
  @DisplayName("Deve retornar lista de cargos com sucesso")
  void deveRetornarListaDeCargos() {
    List<Cargo> cargos = List.of(cargo(1, "Estoquista"), cargo(2, "Gerente"));

    when(cargoRepository.findAll()).thenReturn(cargos);

    try (MockedStatic<CargoMapper> mapper = mockStatic(CargoMapper.class)) {
      mapper.when(() -> CargoMapper.toResponse(cargos.get(0))).thenReturn(response(1, "Estoquista"));
      mapper.when(() -> CargoMapper.toResponse(cargos.get(1))).thenReturn(response(2, "Gerente"));

      List<CargoResponse> resultado = service.findAll();

      assertNotNull(resultado);
      assertEquals(2, resultado.size());
      assertEquals("Estoquista", resultado.get(0).nome());
    }

    verify(cargoRepository).findAll();
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando não há cargos cadastrados")
  void deveRetornarListaVaziaQuandoNaoHaCargos() {
    when(cargoRepository.findAll()).thenReturn(List.of());

    List<CargoResponse> resultado = service.findAll();

    assertNotNull(resultado);
    assertEquals(0, resultado.size());
    verify(cargoRepository).findAll();
  }

  @Test
  @DisplayName("Deve retornar cargo pelo id com sucesso")
  void deveRetornarCargoPorId() {
    Cargo cargo = cargo(1, "Estoquista");

    when(cargoRepository.findById(1)).thenReturn(Optional.of(cargo));

    try (MockedStatic<CargoMapper> mapper = mockStatic(CargoMapper.class)) {
      mapper.when(() -> CargoMapper.toResponse(cargo)).thenReturn(response(1, "Estoquista"));

      CargoResponse resultado = service.findById(1);

      assertNotNull(resultado);
      assertEquals("Estoquista", resultado.nome());
    }

    verify(cargoRepository).findById(1);
  }

  @Test
  @DisplayName("Deve lançar RuntimeException quando cargo não encontrado pelo id")
  void deveLancarExcecaoQuandoCargoNaoEncontradoPorId() {
    when(cargoRepository.findById(99)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> service.findById(99));
    verify(cargoRepository).findById(99);
  }

  @Test
  @DisplayName("Deve criar cargo com sucesso")
  void deveCriarCargoComSucesso() {
    CargoRequest req = request("Estoquista");
    Cargo cargo = cargo(1, "Estoquista");

    when(cargoRepository.existsByNomeIgnoreCase("Estoquista")).thenReturn(false);
    when(permissaoRepository.findByNomeIn(Set.of("cadastrar_itens")))
        .thenReturn(List.of(permissao("cadastrar_itens")));
    when(cargoRepository.save(any(Cargo.class))).thenReturn(cargo);

    try (MockedStatic<CargoMapper> mapper = mockStatic(CargoMapper.class)) {
      mapper.when(() -> CargoMapper.toResponse(cargo)).thenReturn(response(1, "Estoquista"));

      CargoResponse resultado = service.create(req);

      assertNotNull(resultado);
      assertEquals("Estoquista", resultado.nome());
    }

    verify(cargoRepository).existsByNomeIgnoreCase("Estoquista");
    verify(cargoRepository).save(any(Cargo.class));
  }

  @Test
  @DisplayName("Deve lançar EntityNomeJaExisteException ao criar cargo com nome duplicado")
  void deveLancarExcecaoAoCriarCargoComNomeDuplicado() {
    when(cargoRepository.existsByNomeIgnoreCase("Estoquista")).thenReturn(true);

    assertThrows(EntityNomeJaExisteException.class, () -> service.create(request("Estoquista")));
    verify(cargoRepository).existsByNomeIgnoreCase("Estoquista");
  }

  @Test
  @DisplayName("Deve lançar RuntimeException ao criar cargo com permissão inexistente")
  void deveLancarExcecaoAoCriarCargoComPermissaoInexistente() {
    when(cargoRepository.existsByNomeIgnoreCase("Estoquista")).thenReturn(false);
    when(permissaoRepository.findByNomeIn(Set.of("cadastrar_itens"))).thenReturn(List.of());

    assertThrows(RuntimeException.class, () -> service.create(request("Estoquista")));
  }

  @Test
  @DisplayName("Deve atualizar cargo com sucesso")
  void deveAtualizarCargoComSucesso() {
    Cargo cargo = cargo(1, "Estoquista");
    CargoRequest req = request("Estoquista Sênior");

    when(cargoRepository.findById(1)).thenReturn(Optional.of(cargo));
    when(cargoRepository.existsByNomeIgnoreCase("Estoquista Sênior")).thenReturn(false);
    when(permissaoRepository.findByNomeIn(Set.of("cadastrar_itens")))
        .thenReturn(List.of(permissao("cadastrar_itens")));
    when(cargoRepository.save(any(Cargo.class))).thenReturn(cargo);

    try (MockedStatic<CargoMapper> mapper = mockStatic(CargoMapper.class)) {
      mapper.when(() -> CargoMapper.toResponse(cargo)).thenReturn(response(1, "Estoquista Sênior"));

      CargoResponse resultado = service.update(1, req);

      assertNotNull(resultado);
      assertEquals("Estoquista Sênior", resultado.nome());
    }

    verify(cargoRepository).save(any(Cargo.class));
  }

  @Test
  @DisplayName("Deve lançar RuntimeException ao atualizar cargo inexistente")
  void deveLancarExcecaoAoAtualizarCargoInexistente() {
    when(cargoRepository.findById(99)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> service.update(99, request("Qualquer")));
  }

  @Test
  @DisplayName("Deve lançar EntityNomeJaExisteException ao atualizar com nome já usado por outro cargo")
  void deveLancarExcecaoAoAtualizarComNomeDuplicado() {
    Cargo cargo = cargo(1, "Estoquista");

    when(cargoRepository.findById(1)).thenReturn(Optional.of(cargo));
    when(cargoRepository.existsByNomeIgnoreCase("Gerente")).thenReturn(true);

    assertThrows(EntityNomeJaExisteException.class,
        () -> service.update(1, request("Gerente")));
  }

  @Test
  @DisplayName("Deve permitir update mantendo o mesmo nome do cargo")
  void devePermitirUpdateMantentoMesmoNome() {
    Cargo cargo = cargo(1, "Estoquista");
    CargoRequest req = request("Estoquista");

    when(cargoRepository.findById(1)).thenReturn(Optional.of(cargo));
    when(permissaoRepository.findByNomeIn(Set.of("cadastrar_itens")))
        .thenReturn(List.of(permissao("cadastrar_itens")));
    when(cargoRepository.save(any(Cargo.class))).thenReturn(cargo);

    try (MockedStatic<CargoMapper> mapper = mockStatic(CargoMapper.class)) {
      mapper.when(() -> CargoMapper.toResponse(cargo)).thenReturn(response(1, "Estoquista"));

      CargoResponse resultado = service.update(1, req);
      assertNotNull(resultado);
    }

    verify(cargoRepository).save(any(Cargo.class));
  }

  @Test
  @DisplayName("Deve deletar cargo com sucesso")
  void deveDeletarCargo() {
    service.delete(1);

    verify(cargoRepository).deleteById(1);
  }
}
