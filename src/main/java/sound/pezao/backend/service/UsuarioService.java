package sound.pezao.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import sound.pezao.backend.dto.usuarioDTO.UsuarioMapper;
import sound.pezao.backend.dto.usuarioDTO.UsuarioRequest;
import sound.pezao.backend.dto.usuarioDTO.UsuarioResponse;
import sound.pezao.backend.dto.usuarioDTO.UsuarioCadastroResponse;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.CargoRepository;
import sound.pezao.backend.repository.UsuarioRepository;

@PreAuthorize("hasAuthority('GERENCIAR_USUARIOS')")
@Service
public class UsuarioService {

    final UsuarioRepository usuarioRepository;
    final CargoRepository cargoRepository;
    final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, CargoRepository cargoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UsuarioResponse> listar(String search, Integer cargoId, Pageable pageable){
        String busca = search != null && !search.isBlank() ? search.trim() : null;

        return usuarioRepository.findAllFiltered(busca, cargoId, pageable)
                .map(UsuarioMapper::toResponse);
    }

    public UsuarioResponse listar(int id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));
        return UsuarioMapper.toResponse(usuario);
    }

    public UsuarioCadastroResponse cadastrar(UsuarioRequest usuarioRequest){

        if (usuarioRepository.existsByEmailIgnoreCase(usuarioRequest.email())){
            throw new EntityNomeJaExisteException("Usuario", usuarioRequest.email());
        }
        Cargo cargo = cargoRepository.findById(usuarioRequest.cargo_id())
                .orElseThrow(() -> new EntityNotFoundException("Cargo", usuarioRequest.cargo_id()));

        Usuario usuario = UsuarioMapper.toEntity(usuarioRequest);
        usuario.setCargo(cargo);
        usuario.setAtivo(usuarioRequest.ativo() == null || usuarioRequest.ativo());

        // Com senha informada pelo administrador, ela vale e nada é devolvido no
        // response: quem cadastrou já a conhece.
        if (usuarioRequest.temSenha()) {
            usuario.setSenhaHash(passwordEncoder.encode(usuarioRequest.senha()));
            Usuario salvo = usuarioRepository.save(usuario);

            return new UsuarioCadastroResponse(
                    salvo.getId(),
                    salvo.getNome(),
                    salvo.getEmail(),
                    null,
                    salvo.getCriadoEm()
            );
        }

        // Sem senha informada, o usuário recebe a senha padrão derivada do id, que
        // é devolvida uma única vez para ser compartilhada com ele.
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        String senhaPadrao = gerarSenhaPadraoDoUsuario(usuarioSalvo.getId());
        usuarioSalvo.setSenhaHash(passwordEncoder.encode(senhaPadrao));
        usuarioRepository.save(usuarioSalvo);

        return new UsuarioCadastroResponse(
            usuarioSalvo.getId(),
            usuarioSalvo.getNome(),
            usuarioSalvo.getEmail(),
            senhaPadrao,
            usuarioSalvo.getCriadoEm()
        );
    }

    private String gerarSenhaPadraoDoUsuario(Integer usuarioId) {
        return "Pezao_" + String.format("%04d", usuarioId);
    }

    public UsuarioResponse atualizar(
            int id,
            UsuarioRequest usuarioRequest
    ){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(usuarioRequest.email(), id)){
            throw new EntityNomeJaExisteException("Email", usuarioRequest.email());
        }

        Cargo cargo = cargoRepository.findById(usuarioRequest.cargo_id())
                .orElseThrow(() -> new EntityNotFoundException("Cargo", usuarioRequest.cargo_id()));

        usuario.setNome(usuarioRequest.nome());
        usuario.setEmail(usuarioRequest.email());
        usuario.setCargo(cargo);

        if (usuarioRequest.ativo() != null) {
            usuario.setAtivo(usuarioRequest.ativo());
        }

        // A senha só é tocada quando vem preenchida: editar nome ou cargo não pode
        // trocar a senha de ninguém.
        if (usuarioRequest.temSenha()) {
            usuario.setSenhaHash(passwordEncoder.encode(usuarioRequest.senha()));
        }

        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    public void ativar(int id){

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));

        usuario.setAtivo(!usuario.isAtivo());
        usuarioRepository.save(usuario);
    }

}
