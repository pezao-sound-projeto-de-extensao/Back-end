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
import sound.pezao.backend.exception.EntityNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @Test
    @DisplayName("Deve retornar EntityNotFoundException quando tentar atualizar unidade inexistente")
    void deveRetornarEntityNotFoundExceptionQuandoTentarAtualizarUnidadeInexistente(){
        var request = new UnidadeRequest("Teste 01", "T1");
        Optional<Unidade> unidadeVazia = Optional.empty();

        Mockito.when(repository.findById(1)).thenReturn(unidadeVazia);
        
        Assertions.assertThrows(EntityNotFoundException.class, () -> service.update(1, request));
    }

    @Test
    @DisplayName("Deve retornar EntityNomeJaExisteException quando tentar atualizar unidade para um nome já existente")
    void deveRetornarEntityNomeJaExisteExceptionQuandoTentarAtualizarUnidadeParaNomeJaExistente(){
        var request = new UnidadeRequest("Teste 01", "T1");
        var unidadeExistente = new Unidade(1, "Teste Atual", "TA");
        Optional<Unidade> unidadeOptional = Optional.of(unidadeExistente);

        Mockito.when(repository.findById(1)).thenReturn(unidadeOptional);
        Mockito.when(repository.existsByNomeIgnoreCase(request.nome())).thenReturn(true);

        Assertions.assertThrows(EntityNomeJaExisteException.class, () -> service.update(1, request));
        Mockito.verify(repository, Mockito.never()).save(Mockito.any(Unidade.class));
    }

    @Test
    @DisplayName("Deve atualizar unidade com sucesso")
    void deveAtualizarUnidadeComSucesso(){
        var request = new UnidadeRequest("Teste 01", "T1");
        var unidadeExistente = new Unidade(1, "Teste Atual", "TA");
        Optional<Unidade> unidadeOptional = Optional.of(unidadeExistente);

        Mockito.when(repository.findById(1)).thenReturn(unidadeOptional);
        Mockito.when(repository.existsByNomeIgnoreCase(request.nome())).thenReturn(false);
        Mockito.when(repository.save(Mockito.any(Unidade.class))).thenReturn(unidadeExistente);

        UnidadeResponse resultado = service.update(1, request);
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(unidadeExistente.getNome(), resultado.nome());
        Assertions.assertEquals(unidadeExistente.getAbreviacao(), resultado.abreviacao());
    }

    @Test
    @DisplayName("Deve atualizar unidade com sucesso mantendo o mesmo nome")
    void deveAtualizarUnidadeComSucessoMantendoOMesmoNome(){
        var request = new UnidadeRequest("Teste Atual", "XX");
        var unidadeExistente = new Unidade(1, "Teste Atual", "TA");
        Optional<Unidade> unidadeOptional = Optional.of(unidadeExistente);

        Mockito.when(repository.findById(1)).thenReturn(unidadeOptional);
        Mockito.when(repository.save(Mockito.any(Unidade.class))).thenReturn(unidadeExistente);

        UnidadeResponse resultado = service.update(1, request);
        Assertions.assertNotNull(resultado);
        Mockito.verify(repository, Mockito.never()).existsByNomeIgnoreCase(Mockito.anyString());
    }

    @Test
    @DisplayName("Deve retornar EntityNotFoundException quando tentar deletar unidade inexistente")
    void deveRetornarEntityNotFoundExceptionQuandoTentarDeletarUnidadeInexistente(){
        Mockito.when(repository.existsById(1)).thenReturn(false);
        Assertions.assertThrows(EntityNotFoundException.class, () -> service.delete(1));
    }

    @Test
    @DisplayName("Deve deletar unidade com sucesso")
    void deveDeletarUnidadeComSucesso(){
        Mockito.when(repository.existsById(1)).thenReturn(true);
        Mockito.doNothing().when(repository).deleteById(1);

        service.delete(1);
        Mockito.verify(repository, Mockito.times(1)).deleteById(1);
    }
} 