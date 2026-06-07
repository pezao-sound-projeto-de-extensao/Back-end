package sound.pezao.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sound.pezao.backend.dto.permissaoDTO.PermissaoResponse;
import sound.pezao.backend.service.PermissaoService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para PermissaoController")
class PermissaoControllerTest {

  @Mock
  private PermissaoService service;

  @InjectMocks
  private PermissaoController controller;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  private PermissaoResponse response() {
    return new PermissaoResponse("gerenciar_cargos", "responsável por gerenciar os cargos");
  }

  @Test
  @DisplayName("Deve retornar 200 ao listar todas as permissões")
  void deveRetornar200AoListarPermissoes() throws Exception {
    when(service.findAll()).thenReturn(List.of(response()));

    mockMvc.perform(get("/permissoes"))
        .andExpect(status().isOk());

    verify(service).findAll();
  }
}
