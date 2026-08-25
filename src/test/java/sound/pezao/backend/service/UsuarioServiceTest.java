package sound.pezao.backend.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import sound.pezao.backend.dto.usuarioDTO.UsuarioCadastroResponse;
import sound.pezao.backend.dto.usuarioDTO.UsuarioRequest;
import sound.pezao.backend.dto.usuarioDTO.UsuarioResponse;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Permissao;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.exception.EntityNomeJaExisteException;
import sound.pezao.backend.exception.EntityNotFoundException;
import sound.pezao.backend.repository.CargoRepository;
import sound.pezao.backend.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de negócio para UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private Cargo cargo;
    private UsuarioRequest usuarioRequest;

    @BeforeEach
    void setUp() {
        cargo = new Cargo();
        cargo.setId(1);
        cargo.setNome("Administrador");
        cargo.setDescricao("Cargo de teste");

        usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@email.com");
        usuario.setSenhaHash("hash");
        usuario.setAtivo(true);
        usuario.setCargo(cargo);

        usuarioRequest = new UsuarioRequest(
                "Usuário Teste",
                "teste@email.com",
                1
        );
    }

    @Nested
    @DisplayName("Busca usuários com paginação")
    class ListarPaginadoTest {

        @Test
        @DisplayName("Deve retornar página de usuários quando existirem registros")
        void listarDeveRetornarPaginaDeUsuariosQuandoExistiremRegistros() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Usuario> page = new PageImpl<>(List.of(usuario), pageable, 1);

            when(usuarioRepository.findAll(pageable)).thenReturn(page);

            Page<UsuarioResponse> resultado = usuarioService.listar(pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getTotalElements());
            assertEquals("teste@email.com", resultado.getContent().get(0).email());

            verify(usuarioRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Busca usuário pelo id")
    class ListarPorIdTest {

        @Test
        @DisplayName("Deve retornar usuário quando id existe")
        void listarDeveRetornarUsuarioQuandoIdExiste() {
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

            UsuarioResponse resultado = usuarioService.listar(1);

            assertNotNull(resultado);
            assertEquals("teste@email.com", resultado.email());

            verify(usuarioRepository).findById(1);
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando id não existe")
        void listarDeveLancarEntityNotFoundExceptionQuandoIdNaoExiste() {
            when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> usuarioService.listar(1));

            verify(usuarioRepository).findById(1);
        }
    }

    @Nested
    @DisplayName("Cadastra usuário")
    class CadastrarTest {

        @Test
        @DisplayName("Deve cadastrar usuário quando dados válidos")
        void cadastrarDeveCadastrarUsuarioQuandoDadosValidos() {
            when(usuarioRepository.existsByEmailIgnoreCase(usuarioRequest.email())).thenReturn(false);
            when(cargoRepository.findById(usuarioRequest.cargo_id())).thenReturn(Optional.of(cargo));
            when(passwordEncoder.encode("Pezao_0001")).thenReturn("hash-padrao");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                Usuario u = invocation.getArgument(0);
                u.setId(1);
                return u;
            });

            UsuarioCadastroResponse resultado = usuarioService.cadastrar(usuarioRequest);

            assertNotNull(resultado);
            assertEquals(usuarioRequest.email(), resultado.email());

            verify(usuarioRepository).existsByEmailIgnoreCase(usuarioRequest.email());
            verify(cargoRepository).findById(usuarioRequest.cargo_id());
            verify(passwordEncoder).encode("Pezao_0001");
            verify(usuarioRepository, times(2)).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar EntityNomeJaExisteException quando email já existe")
        void cadastrarDeveLancarEntityNomeJaExisteExceptionQuandoEmailJaExiste() {
            when(usuarioRepository.existsByEmailIgnoreCase(usuarioRequest.email())).thenReturn(true);

            assertThrows(EntityNomeJaExisteException.class,
                    () -> usuarioService.cadastrar(usuarioRequest));

            verify(usuarioRepository).existsByEmailIgnoreCase(usuarioRequest.email());
            verify(cargoRepository, never()).findById(anyInt());
            verify(passwordEncoder, never()).encode(any());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando cargo não existe")
        void cadastrarDeveLancarEntityNotFoundExceptionQuandoCargoNaoExiste() {
            when(usuarioRepository.existsByEmailIgnoreCase(usuarioRequest.email())).thenReturn(false);
            when(cargoRepository.findById(usuarioRequest.cargo_id())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> usuarioService.cadastrar(usuarioRequest));

            verify(usuarioRepository).existsByEmailIgnoreCase(usuarioRequest.email());
            verify(cargoRepository).findById(usuarioRequest.cargo_id());
            verify(passwordEncoder, never()).encode(any());
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Atualiza usuário")
    class AtualizarTest {

        @Test
        @DisplayName("Deve atualizar usuário quando dados válidos")
        void atualizarDeveAtualizarUsuarioQuandoDadosValidos() {
            when(usuarioRepository.existsById(1)).thenReturn(true);
            when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot(usuarioRequest.email(), 1)).thenReturn(false);
            when(cargoRepository.findById(usuarioRequest.cargo_id())).thenReturn(Optional.of(cargo));
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UsuarioResponse resultado = usuarioService.atualizar(1, usuarioRequest);

            assertNotNull(resultado);
            assertEquals(usuarioRequest.email(), resultado.email());

            verify(usuarioRepository).existsById(1);
            verify(usuarioRepository).existsByEmailIgnoreCaseAndIdNot(usuarioRequest.email(), 1);
            verify(cargoRepository).findById(usuarioRequest.cargo_id());
            verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando usuário não existe")
        void atualizarDeveLancarEntityNotFoundExceptionQuandoUsuarioNaoExiste() {
            when(usuarioRepository.existsById(1)).thenReturn(false);

            assertThrows(EntityNotFoundException.class,
                    () -> usuarioService.atualizar(1, usuarioRequest));

            verify(usuarioRepository).existsById(1);
            verify(usuarioRepository, never()).existsByEmailIgnoreCaseAndIdNot(anyString(), anyInt());
            verify(cargoRepository, never()).findById(anyInt());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar EntityNomeJaExisteException quando email já existe em outro usuário")
        void atualizarDeveLancarEntityNomeJaExisteExceptionQuandoEmailJaExisteEmOutroUsuario() {
            when(usuarioRepository.existsById(1)).thenReturn(true);
            when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot(usuarioRequest.email(), 1)).thenReturn(true);

            assertThrows(EntityNomeJaExisteException.class,
                    () -> usuarioService.atualizar(1, usuarioRequest));

            verify(usuarioRepository).existsById(1);
            verify(usuarioRepository).existsByEmailIgnoreCaseAndIdNot(usuarioRequest.email(), 1);
            verify(cargoRepository, never()).findById(anyInt());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando cargo não existe")
        void atualizarDeveLancarEntityNotFoundExceptionQuandoCargoNaoExiste() {
            when(usuarioRepository.existsById(1)).thenReturn(true);
            when(usuarioRepository.existsByEmailIgnoreCaseAndIdNot(usuarioRequest.email(), 1)).thenReturn(false);
            when(cargoRepository.findById(usuarioRequest.cargo_id())).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> usuarioService.atualizar(1, usuarioRequest));

            verify(usuarioRepository).existsById(1);
            verify(usuarioRepository).existsByEmailIgnoreCaseAndIdNot(usuarioRequest.email(), 1);
            verify(cargoRepository).findById(usuarioRequest.cargo_id());
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Ativa ou desativa usuário")
    class AtivarTest {

        @Test
        @DisplayName("Deve inverter status para inativo quando usuário está ativo")
        void ativarDeveInverterStatusParaInativoQuandoUsuarioEstaAtivo() {
            usuario.setAtivo(true);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

            usuarioService.ativar(1);

            assertFalse(usuario.isAtivo());
            verify(usuarioRepository).findById(1);
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve inverter status para ativo quando usuário está inativo")
        void ativarDeveInverterStatusParaAtivoQuandoUsuarioEstaInativo() {
            usuario.setAtivo(false);
            when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

            usuarioService.ativar(1);

            assertTrue(usuario.isAtivo());
            verify(usuarioRepository).findById(1);
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando usuário não existe")
        void ativarDeveLancarEntityNotFoundExceptionQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> usuarioService.ativar(1));

            verify(usuarioRepository).findById(1);
            verify(usuarioRepository, never()).save(any());
        }
    }
}

@SpringJUnitConfig(UsuarioServiceSecurityTest.TestConfig.class)
@DisplayName("Testes de segurança da UsuarioService")
class UsuarioServiceSecurityTest {

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        UsuarioService usuarioService(
                UsuarioRepository usuarioRepository,
                CargoRepository cargoRepository,
                PasswordEncoder passwordEncoder
        ) {
            return new UsuarioService(usuarioRepository, cargoRepository, passwordEncoder);
        }
    }

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private CargoRepository cargoRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Resource
    private UsuarioService usuarioService;

    @Nested
    @DisplayName("Acesso ao método")
    class ListarPaginadoSecurityTest {

        @Test
        @WithMockUser(authorities = "GERENCIAR_USUARIOS")
        @DisplayName("Deve permitir quando tem permissão")
        void devePermitirQuandoTemPermissao() {
            Permissao permissao = new Permissao();
            permissao.setId(1);
            permissao.setNome("GERENCIAR_USUARIOS");
            permissao.setDescricao("Permissão de teste");

            Cargo cargo = new Cargo();
            cargo.setId(1);
            cargo.setNome("Administrador");
            cargo.setDescricao("Cargo de teste");
            cargo.getPermissoes().add(permissao);

            Usuario usuario = new Usuario();
            usuario.setId(1);
            usuario.setNome("Usuário Teste");
            usuario.setEmail("teste@email.com");
            usuario.setSenhaHash("hash");
            usuario.setAtivo(true);
            usuario.setCargo(cargo);

            when(usuarioRepository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(usuario)));

            assertDoesNotThrow(() -> usuarioService.listar(Pageable.unpaged()));

            verify(usuarioRepository).findAll(any(Pageable.class));
        }

        @Test
        @WithMockUser(authorities = "OUTRA_PERMISSAO")
        @DisplayName("Deve negar quando não tem permissão")
        void deveNegarQuandoNaoTemPermissao() {
            assertThrows(AccessDeniedException.class,
                    () -> usuarioService.listar(Pageable.unpaged()));

            verify(usuarioRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("Deve negar quando anônimo")
        void deveNegarQuandoAnonimo() {
            assertThrows(AccessDeniedException.class,
                    () -> usuarioService.listar(Pageable.unpaged()));

            verify(usuarioRepository, never()).findAll(any(Pageable.class));
        }
    }
}