package sound.pezao.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sound.pezao.backend.dto.clienteDTO.ClienteRequest;
import sound.pezao.backend.dto.clienteDTO.ClienteResponse;
import sound.pezao.backend.entities.Cliente;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ClienteRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ClienteService")
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteService service;

    private final Pageable pageable = PageRequest.of(0, 20);

    private Cliente cliente() {
        Cliente cliente = new Cliente("João da Silva", "(11) 98888-7777");
        cliente.setId(1);
        return cliente;
    }

    @Test
    @DisplayName("Deve listar clientes repassando a busca")
    void deveListarComBusca() {
        when(repository.findAllFiltered("joão", pageable))
                .thenReturn(new PageImpl<>(List.of(cliente())));

        Page<ClienteResponse> resultado = service.listar("joão", pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("João da Silva", resultado.getContent().get(0).nome());
    }

    @Test
    @DisplayName("Deve cadastrar um cliente")
    void deveCadastrar() {
        when(repository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse resposta = service.cadastrar(new ClienteRequest("Maria Souza", "(11) 97777-6666"));

        assertEquals("Maria Souza", resposta.nome());
        assertEquals("(11) 97777-6666", resposta.telefone());
    }

    @Test
    @DisplayName("Deve atualizar nome e telefone preservando o cadastro")
    void deveAtualizar() {
        Cliente existente = cliente();
        when(repository.findById(1)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        ClienteResponse resposta = service.atualizar(1, new ClienteRequest("João Silva", "(11) 90000-0000"));

        assertEquals("João Silva", resposta.nome());
        assertEquals("(11) 90000-0000", resposta.telefone());
        verify(repository).save(existente);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException para cliente inexistente")
    void deveLancarExcecaoQuandoNaoExiste() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.buscarPorId(99));
    }

    @Test
    @DisplayName("Deve criar a entidade usada pelo fluxo de orçamento")
    void deveCriarEntidade() {
        when(repository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente criado = service.criarEntidade(new ClienteRequest("Carlos Lima", null));

        assertEquals("Carlos Lima", criado.getNome());
    }
}
