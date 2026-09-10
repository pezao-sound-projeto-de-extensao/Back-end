package sound.pezao.backend.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sound.pezao.backend.entities.Cargo;
import sound.pezao.backend.entities.Usuario;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("Testes da listagem filtrada de usuários")
class UsuarioRepositoryFiltroTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final Pageable pageable = PageRequest.of(0, 20);

    private Cargo administrador;
    private Cargo operador;

    @BeforeEach
    void setUp() {
        administrador = persistirCargo("Administrador");
        operador = persistirCargo("Operador");

        persistirUsuario("João da Silva", "joao@email.com", administrador);
        persistirUsuario("Maria Souza", "maria@email.com", operador);
        persistirUsuario("Carlos Lima", "carlos.lima@outroemail.com", operador);
    }

    private Cargo persistirCargo(String nome) {
        Cargo cargo = new Cargo();
        cargo.setNome(nome);
        return entityManager.persist(cargo);
    }

    private void persistirUsuario(String nome, String email, Cargo cargo) {
        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senhaHash("hash")
                .ativo(true)
                .cargo(cargo)
                .build();
        entityManager.persist(usuario);
    }

    private List<String> nomes(Page<Usuario> pagina) {
        return pagina.getContent().stream().map(Usuario::getNome).toList();
    }

    @Test
    @DisplayName("Deve devolver todos os usuários quando não há filtro")
    void deveDevolverTodosSemFiltro() {
        assertEquals(3, usuarioRepository.findAllFiltered(null, null, pageable).getTotalElements());
    }

    @Test
    @DisplayName("Deve buscar por parte do nome ignorando a caixa")
    void deveBuscarPorNome() {
        assertEquals(List.of("Maria Souza"),
                nomes(usuarioRepository.findAllFiltered("maria", null, pageable)));
    }

    @Test
    @DisplayName("Deve buscar por parte do e-mail")
    void deveBuscarPorEmail() {
        assertEquals(List.of("Carlos Lima"),
                nomes(usuarioRepository.findAllFiltered("outroemail", null, pageable)));
    }

    @Test
    @DisplayName("Deve filtrar por cargo")
    void deveFiltrarPorCargo() {
        Page<Usuario> resultado = usuarioRepository.findAllFiltered(null, operador.getId(), pageable);

        assertEquals(2, resultado.getTotalElements());
        assertTrue(nomes(resultado).containsAll(List.of("Maria Souza", "Carlos Lima")));
    }

    @Test
    @DisplayName("Deve combinar busca e cargo")
    void deveCombinarFiltros() {
        assertEquals(List.of("Maria Souza"),
                nomes(usuarioRepository.findAllFiltered("maria", operador.getId(), pageable)));

        assertTrue(usuarioRepository.findAllFiltered("maria", administrador.getId(), pageable).isEmpty());
    }

    @Test
    @DisplayName("Deve indicar se existe usuário vinculado a um cargo")
    void deveIndicarCargoEmUso() {
        assertTrue(usuarioRepository.existsByCargo_Id(administrador.getId()));
        assertTrue(usuarioRepository.existsByCargo_Id(operador.getId()));
    }
}
