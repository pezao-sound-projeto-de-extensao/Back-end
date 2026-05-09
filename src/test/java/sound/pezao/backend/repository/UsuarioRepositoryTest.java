package sound.pezao.backend.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Permissao;
import sound.pezao.backend.entities.Usuario;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Testes de negócio para UsuarioRepository")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Cargo criarCargoComPermissao() {
        Permissao permissao = new Permissao();
        permissao.setNome("GERENCIAR_USUARIOS");
        permissao.setDescricao("Permissão de teste");
        entityManager.persist(permissao);

        Cargo cargo = new Cargo();
        cargo.setNome("Administrador");
        cargo.setDescricao("Cargo de teste");
        cargo.getPermissoes().add(permissao);
        entityManager.persist(cargo);

        entityManager.flush();
        return cargo;
    }

    private Usuario criarUsuario(String email, String senhaHash) {
        Cargo cargo = criarCargoComPermissao();

        Usuario usuario = new Usuario();
        usuario.setNome("Usuário Teste");
        usuario.setEmail(email);
        usuario.setSenhaHash(senhaHash);
        usuario.setCargo(cargo);

        entityManager.persist(usuario);
        entityManager.flush();
        entityManager.clear();

        return usuario;
    }

    @Nested
    @DisplayName("Verifica se o email existe")
    class ExistsByEmailIgnoreCaseTest {

        @Test
        @DisplayName("Deve retornar true quando email existe ignorando case")
        void existsByEmailIgnoreCaseDeveRetornarTrueQuandoEmailExisteIgnorandoCase() {
            criarUsuario("teste@email.com", "hash");

            boolean exists = usuarioRepository.existsByEmailIgnoreCase("TESTE@EMAIL.COM");

            assertTrue(exists);
        }

        @Test
        @DisplayName("Deve retornar false quando email não existe")
        void existsByEmailIgnoreCaseDeveRetornarFalseQuandoEmailNaoExiste() {
            boolean exists = usuarioRepository.existsByEmailIgnoreCase("naoexiste@email.com");

            assertFalse(exists);
        }
    }

    @Nested
    @DisplayName("Verifica se o email existe, exceto o usuário atual")
    class ExistsByEmailIgnoreCaseAndIdNotTest {

        @Test
        @DisplayName("Deve retornar true quando email existe em outro id")
        void existsByEmailIgnoreCaseAndIdNotDeveRetornarTrueQuandoEmailExisteEmOutroId() {
            Usuario usuario = criarUsuario("teste@email.com", "hash");

            boolean exists = usuarioRepository
                    .existsByEmailIgnoreCaseAndIdNot("TESTE@EMAIL.COM", usuario.getId() + 1);

            assertTrue(exists);
        }

        @Test
        @DisplayName("Deve retornar false quando email pertence ao mesmo id")
        void existsByEmailIgnoreCaseAndIdNotDeveRetornarFalseQuandoEmailPertenceAoMesmoId() {
            Usuario usuario = criarUsuario("teste@email.com", "hash");

            boolean exists = usuarioRepository
                    .existsByEmailIgnoreCaseAndIdNot("TESTE@EMAIL.COM", usuario.getId());

            assertFalse(exists);
        }

        @Test
        @DisplayName("Deve retornar false quando email não existe")
        void existsByEmailIgnoreCaseAndIdNotDeveRetornarFalseQuandoEmailNaoExiste() {
            boolean exists = usuarioRepository
                    .existsByEmailIgnoreCaseAndIdNot("naoexiste@email.com", 999);

            assertFalse(exists);
        }
    }

    @Nested
    @DisplayName("Busca usuário por email")
    class FindByEmailTest {

        @Test
        @DisplayName("Deve retornar usuário quando email existe")
        void findByEmailDeveRetornarUsuarioQuandoEmailExiste() {
            criarUsuario("teste@email.com", "hash");

            Optional<Usuario> resultado = usuarioRepository.findByEmail("teste@email.com");

            assertTrue(resultado.isPresent());
            assertEquals("teste@email.com", resultado.get().getEmail());
        }

        @Test
        @DisplayName("Deve retornar vazio quando email não existe")
        void findByEmailDeveRetornarVazioQuandoEmailNaoExiste() {
            Optional<Usuario> resultado = usuarioRepository.findByEmail("naoexiste@email.com");

            assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    @DisplayName("Busca todas as informações relacionadas ao usuário de acordo com o email")
    class FindByEmailCompletoTest {

        @Test
        @DisplayName("Deve retornar usuário com cargo e permissões quando email existe")
        void findByEmailCompletoDeveRetornarUsuarioComCargoEPermissoesQuandoEmailExiste() {
            criarUsuario("teste@email.com", "hash");

            Optional<Usuario> resultado = usuarioRepository.findByEmailCompleto("teste@email.com");

            assertTrue(resultado.isPresent());
            assertNotNull(resultado.get().getCargo());
            assertNotNull(resultado.get().getCargo().getPermissoes());
            assertFalse(resultado.get().getCargo().getPermissoes().isEmpty());
        }

        @Test
        @DisplayName("Deve retornar vazio quando email não existe")
        void findByEmailCompletoDeveRetornarVazioQuandoEmailNaoExiste() {
            Optional<Usuario> resultado = usuarioRepository.findByEmailCompleto("naoexiste@email.com");

            assertTrue(resultado.isEmpty());
        }
    }

    @Nested
    @DisplayName("Salva o usuário no banco")
    class PersistenciaUsuarioTest {

        @Test
        @DisplayName("Deve definir criadoEm e ativo no prePersist")
        void deveDefinirCriadoEmEAtivoNoPrePersist() {
            Cargo cargo = criarCargoComPermissao();

            Usuario usuario = new Usuario();
            usuario.setNome("Usuário Teste");
            usuario.setEmail("prepersist@email.com");
            usuario.setSenhaHash("hash");
            usuario.setCargo(cargo);

            entityManager.persist(usuario);
            entityManager.flush();

            assertNotNull(usuario.getCriadoEm());
            assertTrue(usuario.isAtivo());
        }
    }
}