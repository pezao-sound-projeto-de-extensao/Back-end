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
import sound.pezao.backend.dto.unidadesDTO.UnidadeRequest;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.repository.UnidadeRepository;
import sound.pezao.backend.exception.EntityNomeJaExisteException;

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

    @Test
    @DisplayName("Deve retornar EntityNomeJaExisteException quando já existir unidade com mesmo")
    void deveRetornarEntityNomeJaExisteExceptionQuandoJaExistirUnidadeComMesmoNome(){
        var request = new UnidadeRequest("Teste 01", "T1");

        Mockito.when(repository.existsByNomeIgnoreCase(request.nome())).thenReturn(true);

        Assertions.assertThrows(EntityNomeJaExisteException.class, () -> service.create(request));
    }

    @Test
    @DisplayName("Deve criar uma nova unidade com sucesso")
    void deveCriarNovaUnidadeComSucesso(){
        var request = new UnidadeRequest("Teste 01", "T1");
        var unidadeSalva = new Unidade(1, request.nome(), request.abreviacao());

        Mockito.when(repository.existsByNomeIgnoreCase(request.nome())).thenReturn(false);
        Mockito.when(repository.save(Mockito.any(Unidade.class))).thenReturn(unidadeSalva);
        
        UnidadeResponse resultado = service.create(request);
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(unidadeSalva.getNome(), resultado.nome());
        Assertions.assertEquals(unidadeSalva.getAbreviacao(), resultado.abreviacao());
    }

    
} 