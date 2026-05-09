package sound.pezao.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cglib.core.Local;

import sound.pezao.backend.dto.categoriaDTO.CategoriaResponse;
import sound.pezao.backend.dto.categoriaDTO.CategoriaRequest;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    @InjectMocks
    private CategoriaService service;
  
    @Test
    @DisplayName("Deve retonar uma lista vázia quando não há dados")
    void deveRetonarListaVaziaQuandoNaoHaDados(){
        var listaEsperada = Collections.EMPTY_LIST;

        Mockito.when(repository.findAll()).thenReturn(listaEsperada);

        List<CategoriaResponse> resultado = service.findAll();

        Assertions.assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar uma lista preenchida quando há dados")
    void deveRetonarListaPreenchiaQuandoHaDados(){
        var listaEsperada = List.of(
          new Categoria(1, "Teste 01", LocalDateTime.now()),
          new Categoria(2, "Teste 02", LocalDateTime.now())
        );

        Mockito.when(repository.findAll()).thenReturn(listaEsperada);

        List<CategoriaResponse> resultado = service.findAll();

        Assertions.assertFalse(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar EntityNomeJaExisteException quando já existir categoria com mesmo")
    void deveRetornarEntityNomeJaExisteExceptionQuandoJaExistirCategoriaComMesmoNome(){
        var request = new CategoriaRequest("Teste 01");

        Mockito.when(repository.existsByNomeIgnoreCase(request.nome())).thenReturn(true);

        Assertions.assertThrows(EntityNomeJaExisteException.class, () -> service.create(request));
    }

    @Test
    @DisplayName("Deve criar uma nova unidade com sucesso")
    void deveCriarNovaCategoriaComSucesso(){
        var request = new CategoriaRequest("Teste 01");
        var categoriaSalva = new Categoria(1, request.nome(), LocalDateTime.now());

        Mockito.when(repository.existsByNomeIgnoreCase(request.nome())).thenReturn(false);
        Mockito.when(repository.save(Mockito.any(Categoria.class))).thenReturn(categoriaSalva);
        
        CategoriaResponse resultado = service.create(request);
        Assertions.assertNotNull(resultado);
        Assertions.assertEquals(categoriaSalva.getNome(), resultado.nome());
    }

    @Test
    @DisplayName("Deve retornar EntityNotFoundException quando tentar atualizar categoria inexistente")
    void deveRetornarEntityNotFoundExceptionQuandoTentarAtualizarCategoriaInexistente(){
        var request = new CategoriaRequest("Teste 01");
        Optional<Categoria> categoriaVazia = Optional.empty();

        Mockito.when(repository.findById(1)).thenReturn(categoriaVazia);
        
        Assertions.assertThrows(EntityNotFoundException.class, () -> service.update(1, request));
    }

//    @Test
//    @DisplayName("Deve retornar EntityNomeJaExisteException quando tentar atualizar categoria para um nome já existente")
//    void deveRetornarEntityNomeJaExisteExceptionQuandoTentarAtualizarCategoriaParaNomeJaExistente(){
//        var request = new CategoriaRequest("Teste 01");
//        var categoriaExistente = new Categoria(1, "Teste Atual", LocalDateTime.now());
//        Optional<Categoria> categoriaOptional = Optional.of(categoriaExistente);
//
//        Mockito.when(repository.findById(1)).thenReturn(categoriaOptional);
//        Mockito.when(repository.existsByNomeIgnoreCase(request.nome())).thenReturn(true);
//
//        Assertions.assertThrows(EntityNomeJaExisteException.class, () -> service.update(1, request));
//        Mockito.verify(repository, Mockito.never()).save(Mockito.any(Categoria.class));
//    }

//    @Test
//    @DisplayName("Deve atualizar categoria com sucesso")
//    void deveAtualizarCategoriaComSucesso(){
//        var request = new CategoriaRequest("Teste 01");
//        var categoriaExistente = new Categoria(1, "Teste Atual", LocalDateTime.now());
//        Optional<Categoria> categoriaOptional = Optional.of(categoriaExistente);
//
//        Mockito.when(repository.findById(1)).thenReturn(categoriaOptional);
//        Mockito.when(repository.existsByNomeIgnoreCase(request.nome())).thenReturn(false);
//        Mockito.when(repository.save(Mockito.any(Categoria.class))).thenReturn(categoriaExistente);
//
//        CategoriaResponse resultado = service.update(1, request);
//        Assertions.assertNotNull(resultado);
//        Assertions.assertEquals(categoriaExistente.getNome(), resultado.nome());
//    }

    @Test
    @DisplayName("Deve retornar EntityNotFoundException quando tentar deletar categoria inexistente")
    void deveRetornarEntityNotFoundExceptionQuandoTentarDeletarCategoriaInexistente(){
        Mockito.when(repository.existsById(1)).thenReturn(false);
        Assertions.assertThrows(EntityNotFoundException.class, () -> service.delete(1));
    }

    @Test
    @DisplayName("Deve deletar categoria com sucesso")
    void deveDeletarCategoriaComSucesso(){
        Mockito.when(repository.existsById(1)).thenReturn(true);
        Mockito.doNothing().when(repository).deleteById(1);

        service.delete(1);
        Mockito.verify(repository, Mockito.times(1)).deleteById(1);
    }
}