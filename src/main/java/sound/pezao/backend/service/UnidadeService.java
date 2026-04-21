package sound.pezao.backend.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import sound.pezao.backend.dto.unidadesDTO.UnidadeMapper;
import sound.pezao.backend.dto.unidadesDTO.UnidadeRequest;
import sound.pezao.backend.dto.unidadesDTO.UnidadeResponse;
import sound.pezao.backend.entities.Unidade;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.UnidadeRepository;

@Service
public class UnidadeService {
    private final UnidadeRepository repository;

    public UnidadeService(UnidadeRepository repository) {
        this.repository = repository;
    }

    public List<UnidadeResponse> findAll() {
        return UnidadeMapper.toResponse(repository.findAll());
    }

    @PreAuthorize("hasAuthority('CADASTRAR_ITENS')")
    public UnidadeResponse create(UnidadeRequest request) {
        if (repository.existsByNomeIgnoreCase((request.nome()))) {
            throw new EntityNomeJaExisteException("Unidade", request.nome());
        }
        Unidade unidade = UnidadeMapper.toEntity(request);
        return UnidadeMapper.toResponse(repository.save(unidade));
    }

    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public UnidadeResponse update(Integer id, UnidadeRequest request) {
        Unidade unidade = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Unidade", id));

        if (!unidade.getNome().equalsIgnoreCase(request.nome()) && repository.existsByNomeIgnoreCase(request.nome())) {
            throw new EntityNomeJaExisteException("Unidade", request.nome());
        }

        unidade.setNome(request.nome());
        unidade.setAbreviacao(request.abreviacao());
        return UnidadeMapper.toResponse(repository.save(unidade));
    }

    @PreAuthorize("hasAuthority('EXCLUIR_ITENS')")
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Unidade", id);
        }
        repository.deleteById(id);
    }
}
