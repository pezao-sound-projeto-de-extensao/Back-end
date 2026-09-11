package sound.pezao.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.clienteDTO.ClienteMapper;
import sound.pezao.backend.dto.clienteDTO.ClienteRequest;
import sound.pezao.backend.dto.clienteDTO.ClienteResponse;
import sound.pezao.backend.entities.Cliente;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.ClienteRepository;

@PreAuthorize("hasAuthority('GERENCIAR_ORCAMENTOS')")
@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public Page<ClienteResponse> listar(String search, Pageable pageable) {
        return repository.findAllFiltered(search, pageable).map(ClienteMapper::toResponse);
    }

    public ClienteResponse buscarPorId(Integer id) {
        return ClienteMapper.toResponse(buscarEntidade(id));
    }

    public ClienteResponse cadastrar(ClienteRequest request) {
        return ClienteMapper.toResponse(repository.save(ClienteMapper.toEntity(request)));
    }

    public ClienteResponse atualizar(Integer id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);
        cliente.setNome(request.nome());
        cliente.setTelefone(request.telefone());
        return ClienteMapper.toResponse(repository.save(cliente));
    }

    /**
     * Usado pelo fluxo de orçamento, que cadastra o cliente junto quando ele
     * ainda não existe.
     */
    public Cliente criarEntidade(ClienteRequest request) {
        return repository.save(ClienteMapper.toEntity(request));
    }

    public Cliente buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente", id));
    }
}
