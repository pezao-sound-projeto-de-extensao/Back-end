package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sound.pezao.backend.dto.permissaoDTO.PermissaoMapper;
import sound.pezao.backend.dto.permissaoDTO.PermissaoResponse;
import sound.pezao.backend.entities.Permissao;
import sound.pezao.backend.repository.PermissaoRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para PermissaoService")
class PermissaoServiceTest {

  @Mock
  private PermissaoRepository repository;

  @InjectMocks
  private PermissaoService service;

  private Permissao permissao(Integer id, String nome, String descricao) {
    Permissao permissao = new Permissao();
    permissao.setId(id);
    permissao.setNome(nome);
    permissao.setDescricao(descricao);
    return permissao;
  }

  private PermissaoResponse response(String nome, String descricao) {
    return new PermissaoResponse(nome, descricao);
  }

  @Test
  @DisplayName("Deve retornar lista de permissões com sucesso")
  void deveRetornarListaDePermissoes() {
    List<Permissao> permissoes = List.of(
        permissao(1, "cadastrar_itens", "Cadastrar novos itens no estoque"),
        permissao(2, "gerenciar_cargos", "Gerenciar cargos dos usuários")
    );
    List<PermissaoResponse> responses = List.of(
        response("cadastrar_itens", "Cadastrar novos itens no estoque"),
        response("gerenciar_cargos", "Gerenciar cargos dos usuários")
    );

    when(repository.findAll()).thenReturn(permissoes);

    try (MockedStatic<PermissaoMapper> mapper = mockStatic(PermissaoMapper.class)) {
      mapper.when(() -> PermissaoMapper.toResponse(permissoes)).thenReturn(responses);

      List<PermissaoResponse> resultado = service.findAll();

      assertNotNull(resultado);
      assertEquals(2, resultado.size());
      assertEquals("cadastrar_itens", resultado.get(0).nome());
      assertEquals("Cadastrar novos itens no estoque", resultado.get(0).descricao());
      assertEquals("gerenciar_cargos", resultado.get(1).nome());
    }

    verify(repository).findAll();
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando não há permissões cadastradas")
  void deveRetornarListaVaziaQuandoNaoHaPermissoes() {
    when(repository.findAll()).thenReturn(List.of());

    try (MockedStatic<PermissaoMapper> mapper = mockStatic(PermissaoMapper.class)) {
      mapper.when(() -> PermissaoMapper.toResponse(List.of())).thenReturn(List.of());

      List<PermissaoResponse> resultado = service.findAll();

      assertNotNull(resultado);
      assertEquals(0, resultado.size());
    }

    verify(repository).findAll();
  }
}
