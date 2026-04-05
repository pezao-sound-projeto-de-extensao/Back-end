package sound.pezao.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sound.pezao.backend.dto.unidadesDTO.UnidadeMapper;
import sound.pezao.backend.dto.unidadesDTO.UnidadeResponse;
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

    public UnidadeResponse findById(Integer id) {
        return repository.findById(id)
                .map(UnidadeMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Unidade", id));
    }
}
