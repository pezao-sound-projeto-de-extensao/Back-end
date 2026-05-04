package sound.pezao.backend.service;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import sound.pezao.backend.dto.alertaDTO.AlertaMapper;
import sound.pezao.backend.dto.alertaDTO.AlertaResponse;
import sound.pezao.backend.repository.AlertaRepository;

import java.util.List;

@Service
public class AlertaService {
    private final AlertaRepository repository;

    public AlertaService(AlertaRepository repository) {
        this.repository = repository;
    }

    public List<AlertaResponse> findAll(){
        return repository.findAll().stream()
                .map(AlertaMapper::toResponse)
                .toList();
    }

    public List<AlertaResponse> findByTipo(String tipo) {
        return repository.findAllByTipo(tipo).stream()
                .map(AlertaMapper::toResponse)
                .toList();
    }
}
