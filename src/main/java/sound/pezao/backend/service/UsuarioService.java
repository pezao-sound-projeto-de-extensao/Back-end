package sound.pezao.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sound.pezao.backend.dto.authDTO.AuthMeResponse;
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

import java.util.UUID;

@PreAuthorize("hasAuthority('GERENCIAR_USUARIOS')")
@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger("eventos");

    final UsuarioRepository usuarioRepository;
    final CargoRepository cargoRepository;
    final PasswordEncoder passwordEncoder;
    final RecuperacaoSenhaService recuperacaoSenhaService;

    public UsuarioService(UsuarioRepository usuarioRepository, CargoRepository cargoRepository, PasswordEncoder passwordEncoder,
                          RecuperacaoSenhaService recuperacaoSenhaService) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
        this.passwordEncoder = passwordEncoder;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
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

    @Transactional
    public UsuarioCadastroResponse cadastrar(UsuarioRequest usuarioRequest){

        if (usuarioRepository.existsByEmailIgnoreCase(usuarioRequest.email())){
            throw new EntityNomeJaExisteException("Usuario", usuarioRequest.email());
        }
        Cargo cargo = cargoRepository.findById(usuarioRequest.cargo_id())
                .orElseThrow(() -> new EntityNotFoundException("Cargo", usuarioRequest.cargo_id()));

        Usuario usuario = UsuarioMapper.toEntity(usuarioRequest);
        usuario.setCargo(cargo);
        usuario.setAtivo(usuarioRequest.ativo() == null || usuarioRequest.ativo());

        String senha = usuarioRequest.temSenha() ? usuarioRequest.senha() : UUID.randomUUID().toString();
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        Usuario salvo = usuarioRepository.save(usuario);

        boolean convidado = !usuarioRequest.temSenha() && salvo.isAtivo();
        if (convidado) {
            recuperacaoSenhaService.convidar(salvo);
        }
        log.info("usuario_criado id={} cargo={} convite={}", salvo.getId(), cargo.getId(), convidado);

        return new UsuarioCadastroResponse(
                salvo.getId(),
                salvo.getNome(),
                salvo.getEmail(),
                convidado,
                salvo.getCriadoEm()
        );
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


        if (usuarioRequest.temSenha()) {
            usuario.setSenhaHash(passwordEncoder.encode(usuarioRequest.senha()));
        }

        log.info("usuario_atualizado id={} cargo={} ativo={} senha_alterada={}", id, cargo.getId(), usuario.isAtivo(), usuarioRequest.temSenha());
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    public void ativar(int id){

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", id));

        usuario.setAtivo(!usuario.isAtivo());
        usuarioRepository.save(usuario);
        log.info("usuario_{} id={}", usuario.isAtivo() ? "ativado" : "inativado", id);
    }

    @PreAuthorize("isAuthenticated()")
    public AuthMeResponse buscarMePorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário", 0));

        return UsuarioMapper.toMeResponse(usuario);
    }

}
