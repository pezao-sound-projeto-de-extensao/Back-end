package sound.pezao.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import sound.pezao.backend.dto.unidadesDTO.UnidadeResponse;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.repository.UnidadeRepository;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class UnidadeServiceTest {

    @Mock
    private UnidadeRepository repository;
    
    @InjectMocks
    private UnidadeService service;

    @Test
    @DisplayName("Deve retonar uma lista vázia quando não há dados")
    void deveRetonarListaVaziaQuandoNaoHaDados(){
        var listaEsperada = Collections.EMPTY_LIST;

        Mockito.when(repository.findAll()).thenReturn(listaEsperada);

        List<UnidadeResponse> resultado = service.findAll();

        Assertions.assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar uma lista preenchida quando há dados")
    void deveRetonarListaPreenchiaQuandoHaDados(){
        var listaEsperada = List.of(
          new Unidade(1, "Teste 01", "T1"),
          new Unidade(2, "Teste 02", "T2")
        );

        Mockito.when(repository.findAll()).thenReturn(listaEsperada);

        List<UnidadeResponse> resultado = service.findAll();

        Assertions.assertFalse(resultado.isEmpty());
    }

} 