package sound.pezao.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import sound.pezao.backend.dto.categoriaDTO.CategoriaMapper;
import sound.pezao.backend.dto.categoriaDTO.CategoriaResponse;
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
}
