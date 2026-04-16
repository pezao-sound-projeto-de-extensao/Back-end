package sound.pezao.backend.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import sound.pezao.backend.dto.categoriaDTO.CategoriaMapper;
import sound.pezao.backend.dto.categoriaDTO.CategoriaRequest;
import sound.pezao.backend.dto.categoriaDTO.CategoriaResponse;
import sound.pezao.backend.entities.Categoria;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.CategoriaRepository;

@Service
public class CategoriaService {
    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaResponse> findAll() {
        return CategoriaMapper.toResponse(repository.findAll());
    }


    @PreAuthorize("hasAuthority('CADASTRAR_ITENS')")
    public CategoriaResponse create(CategoriaRequest request){
        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new EntityNomeJaExisteException("Categoria", request.nome());
        }
        
        Categoria categoria = CategoriaMapper.toEntity(request);
        return CategoriaMapper.toResponse(repository.save(categoria));  
    }

    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public CategoriaResponse update(Integer id, CategoriaRequest request){
        Categoria categoria = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria", id));

        categoria.setNome(request.nome());
        repository.save(categoria);
        return CategoriaMapper.toResponse(categoria);
    }

    @PreAuthorize("hasAuthority('EDITAR_ITENS')")
    public void delete(Integer id){
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Categoria", id);
        }
        repository.deleteById(id);
    }
}
