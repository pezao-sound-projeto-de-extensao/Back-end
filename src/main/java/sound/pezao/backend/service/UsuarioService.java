package sound.pezao.backend.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import sound.pezao.backend.dto.ususarioDTO.UsuarioMapper;
import sound.pezao.backend.dto.ususarioDTO.UsuarioRequest;
import sound.pezao.backend.dto.ususarioDTO.UsuarioResponse;
import sound.pezao.backend.repository.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioService {

    final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponse> listar(){
        return UsuarioMapper.toResponse(usuarioRepository.findAll());
    }
    public UsuarioResponse cadastrar(@Valid @RequestBody UsuarioRequest usuarioRequest){

    return null;
    }
}
