package sound.pezao.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import sound.pezao.backend.dto.ususarioDTO.UsuarioMapper;
import sound.pezao.backend.dto.ususarioDTO.UsuarioRequest;
import sound.pezao.backend.dto.ususarioDTO.UsuarioResponse;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.CargoRepository;
import sound.pezao.backend.repository.UsuarioRepository;

import java.util.List;
@PreAuthorize("hasAuthority('GERENCIAR_USUARIOS')")
@Service
public class UsuarioService {

    final UsuarioRepository usuarioRepository;
    final CargoRepository cargoRepository;
    final PasswordEncoder passwordEncoder;

    final static String senhaPadrao = ("PezaoSenha");
    public UsuarioService(UsuarioRepository usuarioRepository, CargoRepository cargoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UsuarioResponse> listar(Pageable pageable){
        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);
        return usuarios.map(UsuarioMapper::toResponse);
    }

    public UsuarioResponse listar(int id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));
        return UsuarioMapper.toResponse(usuario);
    }

    public UsuarioResponse cadastrar(@RequestBody UsuarioRequest usuarioRequest){

        if (usuarioRepository.existsByEmailIgnoreCase(usuarioRequest.email())){
            throw new EntityNomeJaExisteException("Usuario", usuarioRequest.email());
        }
        Cargo cargo = cargoRepository.findById(usuarioRequest.cargo_id())
                .orElseThrow(() -> new EntityNotFoundException("Cargo", usuarioRequest.cargo_id()));

        Usuario usuario = UsuarioMapper.toEntity(usuarioRequest);
        usuario.setCargo(cargo);
        usuario.setSenhaHash(passwordEncoder.encode(senhaPadrao));
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse atualizar(
            int id,
            UsuarioRequest usuarioRequest
    ){
        if (!usuarioRepository.existsById(id)){
            throw new EntityNotFoundException("Usuário", id);
        }

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(usuarioRequest.email(), id)){
            throw new EntityNomeJaExisteException("Email", usuarioRequest.email());
        }

        Cargo cargo = cargoRepository.findById(usuarioRequest.cargo_id())
                .orElseThrow(() -> new EntityNotFoundException("Cargo", id));

        Usuario usuario = UsuarioMapper.toEntity(usuarioRequest);
        usuario.setCargo(cargo);
        usuario.setId(id);
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    public void ativar(int id){

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));

        usuario.setAtivo(!usuario.isAtivo());
        usuarioRepository.save(usuario);
    }

}
