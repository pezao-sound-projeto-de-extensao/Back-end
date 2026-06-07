package sound.pezao.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sound.pezao.backend.dto.cargoDTO.CargoRequest;
import sound.pezao.backend.dto.cargoDTO.CargoResponse;
import sound.pezao.backend.service.CargoService;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CargoController")
class CargoControllerTest {

  @Mock
  private CargoService service;

  @InjectMocks
  private CargoController controller;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    objectMapper = new ObjectMapper();
  }

  private CargoResponse response() {
    return new CargoResponse(1, "Estoquista", "Descrição de Estoquista", Set.of("cadastrar_itens"));
  }

  private CargoRequest request() {
    return new CargoRequest("Estoquista", "Descrição de Estoquista", Set.of("cadastrar_itens"));
  }

  @Test
  @DisplayName("Deve retornar 200 ao listar todos os cargos")
  void deveRetornar200AoListarCargos() throws Exception {
    when(service.findAll()).thenReturn(List.of(response()));

    mockMvc.perform(get("/cargos"))
        .andExpect(status().isOk());

    verify(service).findAll();
  }

  @Test
  @DisplayName("Deve retornar 200 ao buscar cargo por id")
  void deveRetornar200AoBuscarCargoPorId() throws Exception {
    when(service.findById(1)).thenReturn(response());

    mockMvc.perform(get("/cargos/{id}", 1))
        .andExpect(status().isOk());

    verify(service).findById(1);
  }

  @Test
  @DisplayName("Deve retornar 201 ao criar um cargo")
  void deveRetornar201AoCriarCargo() throws Exception {
    when(service.create(any(CargoRequest.class))).thenReturn(response());

    mockMvc.perform(post("/cargos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request())))
        .andExpect(status().isCreated());

    verify(service).create(any(CargoRequest.class));
  }

  @Test
  @DisplayName("Deve retornar 200 ao atualizar um cargo")
  void deveRetornar200AoAtualizarCargo() throws Exception {
    when(service.update(eq(1), any(CargoRequest.class))).thenReturn(response());

    mockMvc.perform(put("/cargos/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request())))
        .andExpect(status().isOk());

    verify(service).update(eq(1), any(CargoRequest.class));
  }

  @Test
  @DisplayName("Deve retornar 204 ao deletar um cargo")
  void deveRetornar204AoDeletarCargo() throws Exception {
    doNothing().when(service).delete(1);

    mockMvc.perform(delete("/cargos/{id}", 1))
        .andExpect(status().isNoContent());

    verify(service).delete(1);
  }
}