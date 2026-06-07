package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;
import sound.pezao.backend.dto.alertaDTO.AlertaResponse;
import sound.pezao.backend.entities.Alerta;
import sound.pezao.backend.repository.AlertaRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {
    @Mock
    private AlertaRepository repository;

    @InjectMocks
    private AlertaService service;

    @Nested
    @DisplayName("findAll")
    class findAll {
        @Test
        @DisplayName("Deve retornar lista vazia quando chamado")
        void deveRetornarListaVazia() {
            var listaEsperada = List.of();

            Mockito.when(repository.findAll()).thenReturn(List.of());

            List<AlertaResponse> resultado = service.findAll();
            assertEquals(listaEsperada, resultado);
        }

        @Test
        @DisplayName("Deve retornar alertas corretamente quando chamado")
        void deveRetornarAlertas() {
            var listaEsperada = List.of(
                    new AlertaResponse(
                            10,
                            "Pneu 175/70 R13",
                            "Pneus",
                            "UN",
                            1,
                            4,
                            "estoque_baixo"
                    ),
                    new AlertaResponse(
                            11,
                            "Óleo 5W30",
                            "Lubrificantes",
                            "L",
                            0,
                            10,
                            "zerado"
                    )
            );

            Mockito.when(repository.findAll()).thenReturn(
                    List.of(
                            new Alerta(
                                    10,
                                    "Pneu 175/70 R13",
                                    "Pneus",
                                    "UN",
                                    1,
                                    4,
                                    "estoque_baixo"
                            ),
                            new Alerta(
                                    11,
                                    "Óleo 5W30",
                                    "Lubrificantes",
                                    "L",
                                    0,
                                    10,
                                    "zerado"
                            )
                    )
            );

            List<AlertaResponse> resultado = service.findAll();
            assertEquals(listaEsperada, resultado);
        }
    }

    @Nested
    @DisplayName("findByTipo")
    class findByTipo {
        @Test
        @DisplayName("Deve retornar lista vazia quando chamado")
        void deveRetornarListaVazia() {
            var listaEsperada = List.of();

            Mockito.when(repository.findAllByTipoAlerta("")).thenReturn(List.of());

            List<AlertaResponse> resultado = service.findByTipo("");
            assertEquals(listaEsperada, resultado);
        }

        @Test
        @DisplayName("Deve retornar alertas apenas zerado quando chamado")
        void deveRetornarAlertasZerados() {
            var listaEsperada = List.of(
                    new AlertaResponse(
                            11,
                            "Óleo 5W30",
                            "Lubrificantes",
                            "L",
                            0,
                            10,
                            "zerado"
                    )
            );

            Mockito.when(repository.findAllByTipoAlerta("zerado")).thenReturn(
                    List.of(
                            new Alerta(
                                    11,
                                    "Óleo 5W30",
                                    "Lubrificantes",
                                    "L",
                                    0,
                                    10,
                                    "zerado"
                            )
                    )
            );

            List<AlertaResponse> resultado = service.findByTipo("zerado");
            assertEquals(listaEsperada, resultado);
        }

        @Test
        @DisplayName("Deve retornar alertas apenas estoque_baixo quando chamado")
        void deveRetornarAlertasEstoqueBaixo() {
            var listaEsperada = List.of(
                    new AlertaResponse(
                            11,
                            "Óleo 5W30",
                            "Lubrificantes",
                            "L",
                            0,
                            10,
                            "estoque_baixo"
                    )
            );

            Mockito.when(repository.findAllByTipoAlerta("estoque_baixo")).thenReturn(
                    List.of(
                            new Alerta(
                                    11,
                                    "Óleo 5W30",
                                    "Lubrificantes",
                                    "L",
                                    0,
                                    10,
                                    "estoque_baixo"
                            )
                    )
            );

            List<AlertaResponse> resultado = service.findByTipo("estoque_baixo");
            assertEquals(listaEsperada, resultado);
        }
    }
}