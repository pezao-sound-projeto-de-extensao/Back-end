package sound.pezao.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Permissao;
import sound.pezao.backend.entities.Usuario;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserAuthenticated")
class UserAuthenticatedTest {

    private Usuario usuario;
    private UserAuthenticated userAuthenticated;

    @BeforeEach
    void setUp() {
        Permissao permissao1 = new Permissao();
        permissao1.setId(1);
        permissao1.setNome("GERENCIAR_USUARIOS");
        permissao1.setDescricao("Permissão 1");

        Permissao permissao2 = new Permissao();
        permissao2.setId(2);
        permissao2.setNome("VER_RELATORIOS");
        permissao2.setDescricao("Permissão 2");

        Cargo cargo = new Cargo();
        cargo.setId(1);
        cargo.setNome("Administrador");
        cargo.setDescricao("Cargo de teste");
        cargo.getPermissoes().add(permissao1);
        cargo.getPermissoes().add(permissao2);

        usuario = new Usuario();
        usuario.setId(1);
        usuario.setEmail("teste@email.com");
        usuario.setSenhaHash("senha-hash");
        usuario.setCargo(cargo);
        usuario.setAtivo(true);

        userAuthenticated = new UserAuthenticated(usuario);
    }

    @Nested
    @DisplayName("Retornar as permissões do usuário")
    class GetAuthoritiesTest {

        @Test
        @DisplayName("Deve retornar authorities quando usuário possui permissões")
        void getAuthoritiesDeveRetornarAuthoritiesQuandoUsuarioPossuiPermissoes() {
            Collection<? extends GrantedAuthority> authorities = userAuthenticated.getAuthorities();

            assertNotNull(authorities);
            assertEquals(2, authorities.size());
            assertTrue(authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("GERENCIAR_USUARIOS")));
            assertTrue(authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("VER_RELATORIOS")));
        }
    }

    @Nested
    @DisplayName("Buscar a senha do usuário")
    class GetPasswordTest {

        @Test
        @DisplayName("Deve retornar senha hash quando usuário possui senha")
        void getPasswordDeveRetornarSenhaHashQuandoUsuarioPossuiSenha() {
            String password = userAuthenticated.getPassword();

            assertEquals("senha-hash", password);
        }
    }

    @Nested
    @DisplayName("Buscar o username do usuário")
    class GetUsernameTest {

        @Test
        @DisplayName("Deve retornar username quando usuário possui email")
        void getUsernameDeveRetornarUsernameQuandoUsuarioPossuiEmail() {
            String username = userAuthenticated.getUsername();

            assertEquals("teste@email.com", username);
        }
    }

    @Nested
    @DisplayName("Verificar se a conta não está expirada")
    class IsAccountNonExpiredTest {

        @Test
        @DisplayName("Deve retornar true")
        void isAccountNonExpiredDeveRetornarTrue() {
            assertTrue(userAuthenticated.isAccountNonExpired());
        }
    }

    @Nested
    @DisplayName("Verificar se a conta não está bloqueada")
    class IsAccountNonLockedTest {

        @Test
        @DisplayName("Deve retornar true")
        void isAccountNonLockedDeveRetornarTrue() {
            assertTrue(userAuthenticated.isAccountNonLocked());
        }
    }

    @Nested
    @DisplayName("Verificar se as credenciais da conta não estão expirados")
    class IsCredentialsNonExpiredTest {

        @Test
        @DisplayName("Deve retornar true")
        void isCredentialsNonExpiredDeveRetornarTrue() {
            assertTrue(userAuthenticated.isCredentialsNonExpired());
        }
    }

    @Nested
    @DisplayName("Verificar se o usuário está ativo")
    class IsEnabledTest {

        @Test
        @DisplayName("Deve retornar true quando usuário está ativo")
        void isEnabledDeveRetornarTrueQuandoUsuarioEstaAtivo() {
            assertTrue(userAuthenticated.isEnabled());
        }

        @Test
        @DisplayName("Deve retornar false quando usuário está inativo")
        void isEnabledDeveRetornarFalseQuandoUsuarioEstaInativo() {
            usuario.setAtivo(false);

            UserAuthenticated userInativo = new UserAuthenticated(usuario);

            assertFalse(userInativo.isEnabled());
        }
    }
}