package sound.pezao.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Permissao;
import sound.pezao.backend.entities.Usuario;
import sound.pezao.backend.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de negócio para a UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        Permissao permissao = new Permissao();
        permissao.setId(1);
        permissao.setNome("GERENCIAR_USUARIOS");
        permissao.setDescricao("Permissão de teste");

        Cargo cargo = new Cargo();
        cargo.setId(1);
        cargo.setNome("Administrador");
        cargo.setDescricao("Cargo de teste");
        cargo.getPermissoes().add(permissao);

        usuario = new Usuario();
        usuario.setId(1);
        usuario.setEmail("teste@email.com");
        usuario.setSenhaHash("hash");
        usuario.setCargo(cargo);
        usuario.setAtivo(true);
    }

    @Nested
    @DisplayName("Carregar usuário de acordo com o username")
    class LoadUserByUsernameTest {

        @Test
        @DisplayName("Deve retornar UserDetails quando usuário existe")
        void loadUserByUsernameDeveRetornarUserDetailsQuandoUsuarioExiste() {
            when(userRepository.findByEmailCompleto("teste@email.com"))
                    .thenReturn(Optional.of(usuario));

            UserDetails userDetails = userDetailsService.loadUserByUsername("teste@email.com");

            assertNotNull(userDetails);
            assertInstanceOf(UserAuthenticated.class, userDetails);
            assertEquals("teste@email.com", userDetails.getUsername());
            assertEquals("hash", userDetails.getPassword());
            assertTrue(userDetails.isEnabled());

            verify(userRepository).findByEmailCompleto("teste@email.com");
        }

        @Test
        @DisplayName("Deve lançar UsernameNotFoundException quando usuário não existe")
        void loadUserByUsernameDeveLancarUsernameNotFoundExceptionQuandoUsuarioNaoExiste() {
            when(userRepository.findByEmailCompleto("teste@email.com"))
                    .thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userDetailsService.loadUserByUsername("teste@email.com")
            );

            assertEquals("Usuário não encontrado", ex.getMessage());
            verify(userRepository).findByEmailCompleto("teste@email.com");
        }
    }
}